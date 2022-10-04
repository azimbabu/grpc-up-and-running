package ecommerce;

import java.io.IOException;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;

public class OrderManagementServer {
  private static final Logger logger = Logger.getLogger(OrderManagementServer.class.getName());

  private Server server;

  public static void main(String[] args) throws InterruptedException, IOException {
    final OrderManagementServer server = new OrderManagementServer();
    server.start();
    server.blockUntilShutdown();
  }

  private void start() throws IOException {
    /* The port on which the server should run */
    int port = 50051;
    server =
        ServerBuilder.forPort(port)
            .addService(ServerInterceptors.intercept(new OrderManagementServiceImpl()))
            .build()
            .start();

    logger.info("Server started, listening on " + port);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.err.println("*** shutting down gRPC server since JVM is shutting down");
                  OrderManagementServer.this.stop();
                  System.err.println("*** server shut dow");
                }));
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  /** Await termination on the main thread since the grpc library uses daemon threads. */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }
}
