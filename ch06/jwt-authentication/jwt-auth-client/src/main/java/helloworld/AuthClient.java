package helloworld;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class AuthClient {
  private static final Logger logger = Logger.getLogger(AuthClient.class.getName());

  private final CallCredentials callCredentials;
  private final GreeterGrpc.GreeterBlockingStub blockingStub;
  private final ManagedChannel channel;

  public AuthClient(CallCredentials callCredentials, String host, int port) {
    channel =
        ManagedChannelBuilder.forAddress(host, port)
            // Channels are secure by default (via SSL/TLS). For this example we disable TLS
            // to avoid needing certificates, but it is recommended to use a secure channel
            // while passing credentials.
            .usePlaintext()
            .build();
    this.callCredentials = callCredentials;
    blockingStub = GreeterGrpc.newBlockingStub(channel);
  }

  public static void main(String[] args) throws InterruptedException {
    String user = "World";
    String clientId = "default-client";
    CallCredentials callCredentials = new JwtCredential(clientId);
    AuthClient authClient = new AuthClient(callCredentials, "localhost", 50001);
    try {
      authClient.greet(user);
    } finally {
      authClient.shutdown();
    }
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
  }

  public String greet(String name) {
    logger.info("Will try to greet " + name + " ...");

    Helloworld.HelloRequest request = Helloworld.HelloRequest.newBuilder().setName(name).build();

    // Use a stub with the given call credentials applied to invoke the RPC.
    Helloworld.HelloReply response =
        blockingStub.withCallCredentials(callCredentials).sayHello(request);

    logger.info("Greeting: " + response.getMessage());
    return response.getMessage();
  }
}
