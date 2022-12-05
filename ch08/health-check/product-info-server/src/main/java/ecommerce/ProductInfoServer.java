package ecommerce;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;
import java.util.logging.Logger;

public class ProductInfoServer {
    private static final Logger logger = Logger.getLogger(ProductInfoServer.class.getName());
    private static final int PORT = 50051;

    private Server server;

    public static void main(String[] args) throws IOException, InterruptedException {
        final ProductInfoServer productInfoServer = new ProductInfoServer();
        productInfoServer.start();
        productInfoServer.blockUntilShutdown();
    }

    private void start() throws IOException {
        /* The port on which the server should run */
        HealthStatusManager healthStatusManager = new HealthStatusManager();
        ProductInfoImpl productInfo = new ProductInfoImpl();
        healthStatusManager.setStatus(productInfo.bindService().getServiceDescriptor().getName(), HealthCheckResponse.ServingStatus.SERVING);
        healthStatusManager.setStatus(ProtoReflectionService.newInstance().bindService().getServiceDescriptor().getName(), HealthCheckResponse.ServingStatus.SERVING);

        server =
                ServerBuilder.forPort(PORT)
                        .addService(productInfo)
                        .addService(healthStatusManager.getHealthService())
                        // Needed to enable server reflection
                        .addService(ProtoReflectionService.newInstance())
                        .build()
                        .start();
        logger.info("Server started, listening up on port=" + PORT);

        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                                    System.err.println("*** shutting down gRPC server since JVM is shutting down");
                                    ProductInfoServer.this.stop();
                                    System.err.println("*** server shut down");
                                }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
