package ecommerce;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class ProductInfoServer {
  private static final int PORT = 50001;
  private static final Logger logger = Logger.getLogger(ProductInfoServer.class.getName());

  private Server server;

  public static void main(String[] args) throws IOException, InterruptedException {
    final ProductInfoServer productInfoServer = new ProductInfoServer();
    productInfoServer.start();
    productInfoServer.blockUntilShutdown();
  }

  private void start() throws IOException {
    File certFile = Paths.get("ch06", "secure-channel", "certs", "server.crt").toFile();
    File keyFile = Paths.get("ch06", "secure-channel", "certs", "server.pem").toFile();

    server =
        ServerBuilder.forPort(PORT)
            .addService(new ProductInfoImpl())
            .useTransportSecurity(certFile, keyFile)
            .build()
            .start();
    logger.info("Server started, listening on: " + PORT);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                  System.err.println("*** shutting down gRPC server since JVM is shutting down");
                  ProductInfoServer.this.stop();
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
