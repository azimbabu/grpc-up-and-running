package ecommerce;

import io.grpc.*;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class OpenTelemetryServerInterceptor implements ServerInterceptor {
    private static final Logger logger =
            Logger.getLogger(OpenTelemetryServerInterceptor.class.getName());

    // it is important to initialize the OpenTelemetry SDK as early as possible in your application's
    // lifecycle.
    private static final OpenTelemetry openTelemetry = OpenTelemetryConfiguration.initOpenTelemetry();

    // Extract the Distributed Context from the gRPC metadata
    private static final TextMapGetter<Metadata> textMapGetter =
            new TextMapGetter<Metadata>() {
                @Override
                public Iterable<String> keys(Metadata carrier) {
                    return carrier.keys();
                }

                @Nullable
                @Override
                public String get(@Nullable Metadata carrier, String key) {
                    Metadata.Key<String> metadataKey = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
                    if (carrier.containsKey(metadataKey)) {
                        return carrier.get(metadataKey);
                    }
                    return "";
                }
            };

    private final Tracer tracer = openTelemetry.getTracer(ProductInfoServer.class.getName());

    private final TextMapPropagator textMapPropagator =
            openTelemetry.getPropagators().getTextMapPropagator();

    private LongCounter rpcCounter;

    public OpenTelemetryServerInterceptor() {
        Meter meter = openTelemetry.getMeterProvider().get(OpenTelemetryServerInterceptor.class.getName());
        rpcCounter = meter.counterBuilder("product.management.server.rpc.count")
                .setDescription("Total number of RPCs handled on the server")
                .setUnit("rpc")
                .build();
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        InetSocketAddress inetSocketAddress =
                (InetSocketAddress) call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        // Extract the Span Context from the metadata of the gRPC request
        Context extractedContext = textMapPropagator.extract(Context.current(), headers, textMapGetter);
        // Build a span based on the received context
        logger.info(
                "spanName="
                        + call.getMethodDescriptor().getFullMethodName()
                        + ", serviceName="
                        + call.getMethodDescriptor().getServiceName());
        Span span =
                tracer
                        .spanBuilder(call.getMethodDescriptor().getFullMethodName())
                        .setParent(extractedContext)
                        .setSpanKind(SpanKind.SERVER)
                        .startSpan();
        // Make the span the current span
        try (Scope scope = span.makeCurrent()) {
            // In this scope, the span is the current/active span
            span.setAttribute("component", "grpc");
            span.setAttribute(
                    SemanticAttributes.RPC_SERVICE, call.getMethodDescriptor().getServiceName());
            span.setAttribute(SemanticAttributes.NET_PEER_NAME, inetSocketAddress.getHostString());
            span.setAttribute(SemanticAttributes.NET_PEER_PORT, inetSocketAddress.getPort());

            rpcCounter.add(1, Attributes.of(SemanticAttributes.RPC_METHOD, call.getMethodDescriptor().getFullMethodName()));
            // Process the gRPC call normally
            return Contexts.interceptCall(io.grpc.Context.current(), call, headers, next);
        } finally {
            span.end();
        }
    }
}
