package ecommerce;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductInfoClient {

    private static final Logger logger = Logger.getLogger(ProductInfoClient.class.getName());
    private final ProductInfoGrpc.ProductInfoBlockingStub blockingStub;
    private final Tracer tracer =
            OpenTelemetryConfiguration.openTelemetry.getTracer(ProductInfoClient.class.getName());
    private String serverHost;
    private int serverPort;
    private ManagedChannel channel;

    public ProductInfoClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.channel =
                ManagedChannelBuilder.forAddress(serverHost, serverPort)
                        // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                        // needing certificates.
                        .usePlaintext()
                        // Intercept the request to tag the span context
                        .intercept(new OpenTelemetryClientInterceptor())
                        .build();
        this.blockingStub = ProductInfoGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) {
        String serverHost = System.getProperty("serverHost", "localhost");
        int serverPort = Integer.parseInt(System.getProperty("serverPort", "50051"));
        logger.info("hostname=" + serverHost + ", port=" + serverPort);
        ProductInfoClient productInfoClient = new ProductInfoClient(serverHost, serverPort);

        try {
            ProductInfoOuterClass.Product product =
                    ProductInfoOuterClass.Product.newBuilder()
                            .setName("Apple iPhone 11")
                            .setDescription(
                                    "Meet Apple iPhone 11. All-new dual-camera system with Ultra Wide and Night mode.")
                            .setPrice(1000.0f)
                            .build();
            String serviceName = "ecommerce.ProductInfo";
            String id =
                    productInfoClient.makeRPCCall(
                            serviceName,
                            serviceName + "/addProduct",
                            () -> productInfoClient.addProduct(product));
            logger.info("Product added, id: " + id);

            ProductInfoOuterClass.Product productFound =
                    productInfoClient.makeRPCCall(
                            serviceName, serviceName + "/getProduct", () -> productInfoClient.getProduct(id));
            logger.info("Product found: " + productFound);
        } finally {
            productInfoClient.stop();
        }
    }

    public void stop() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    public String addProduct(ProductInfoOuterClass.Product product) {
        ProductInfoOuterClass.ProductID productID = blockingStub.addProduct(product);
        return productID.getValue();
    }

    public ProductInfoOuterClass.Product getProduct(String id) {
        ProductInfoOuterClass.ProductID productID =
                ProductInfoOuterClass.ProductID.newBuilder().setValue(id).build();
        return blockingStub.getProduct(productID);
    }

    private <T> T makeRPCCall(String serviceName, String spanName, Supplier<T> supplier) {
        // Start a span
        Span span = tracer.spanBuilder(spanName).setSpanKind(SpanKind.CLIENT).startSpan();
        span.setAttribute("component", "grpc");
        span.setAttribute(SemanticAttributes.RPC_SERVICE, serviceName);
        span.setAttribute(SemanticAttributes.NET_PEER_NAME, serverHost);
        span.setAttribute(SemanticAttributes.NET_PEER_PORT, serverPort);

        // Set the context with the current span
        try (Scope ignored = span.makeCurrent()) {
            try {
                return supplier.get();
            } catch (StatusRuntimeException e) {
                logger.log(Level.WARNING, "RPC failed, status={}", e.getStatus());
                span.setStatus(StatusCode.ERROR, "gRPC status=" + e.getStatus());
            }
        } finally {
            span.end();
        }
        return null;
    }
}
