package helloworld;

import java.io.IOException;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class AuthServer {
  private static final Logger logger = Logger.getLogger(AuthServer.class.getName());
  private static final int PORT = 50001;

  private Server server;

  public static void main(String[] args) throws InterruptedException, IOException {
    final AuthServer authServer = new AuthServer();
    authServer.start();
    authServer.blockUntilShutdown();
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

  private void start() throws IOException {
    server =
        ServerBuilder.forPort(PORT)
            .addService(new GreeterImpl())
            .intercept(new JwtServerInterceptor())
            .build()
            .start();
    logger.info("Server started, listening on " + PORT);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                  System.err.println("*** shutting down gRPC server since JVM is shutting down");
                  AuthServer.this.stop();
                  System.err.println("*** server shut down");
                }));
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }
}
