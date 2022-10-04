package ecommerce;

import java.io.IOException;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class OrderManagementServer {
  private static final Logger logger = Logger.getLogger(OrderManagementServer.class.getName());
  private static final int PORT = 50051;
  private Server server;

  public static void main(String[] args) throws InterruptedException, IOException {
    final OrderManagementServer orderManagementServer = new OrderManagementServer();
    orderManagementServer.start();
    orderManagementServer.blockUntilShutdown();
  }

  private void start() throws IOException {
    /* The port on which the server should run */
    server =
        ServerBuilder.forPort(PORT).addService(new OrderManagementServiceImpl()).build().start();
    logger.info("Server started, listening on port=" + PORT);

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  // Use stderr here since the logger may have been reset by its JVM shutdown hook.
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

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   *
   * @throws InterruptedException
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }
}
