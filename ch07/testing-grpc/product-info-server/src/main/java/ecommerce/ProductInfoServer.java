package ecommerce;

import java.io.IOException;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class ProductInfoServer {
  private static final Logger logger = Logger.getLogger(ProductInfoServer.class.getName());
  private static final int PORT = 50051;

  private Server server;

  public static void main(String[] args) throws IOException, InterruptedException {
    final ProductInfoServer productInfoServer = new ProductInfoServer();
    productInfoServer.start();
    productInfoServer.blockUntilShutdown();
  }

  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  private void start() throws IOException {
    /* The port on which the server should run */
    server = ServerBuilder.forPort(PORT).addService(new ProductInfoImpl()).build().start();
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
}
