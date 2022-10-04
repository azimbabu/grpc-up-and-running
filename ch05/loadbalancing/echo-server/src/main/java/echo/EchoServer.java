package echo;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class EchoServer {
  private static final Logger logger = Logger.getLogger(EchoServer.class.getName());

  public static void main(String[] args) {
    final int numServers = 3;
    ExecutorService executorService = Executors.newFixedThreadPool(numServers);
    for (int i = 0; i < numServers; i++) {
      String name = "Server_" + i;
      int port = 50000 + i;
      executorService.submit(
          () -> {
            try {
              startServer(name, port);
            } catch (IOException e) {
              e.printStackTrace();
            }
          });
    }
  }

  private static void startServer(String name, int port) throws IOException {
    Server server = ServerBuilder.forPort(port).addService(new EchoService(name)).build();
    server.start();

    logger.info(String.format("Server %s started, listening on %d", name, port));

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.err.printf(
                      "*** shutting down gRPC server %s since JVM is shutting down", name);
                  stopServer(server);
                  System.err.printf("*** server %s shut dow", name);
                }));
  }

  private static void stopServer(Server server) {
    if (server != null) {
      server.shutdown();
    }
  }
}
