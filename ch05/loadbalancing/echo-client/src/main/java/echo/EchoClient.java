package echo;

import java.net.InetSocketAddress;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolverRegistry;

public class EchoClient {
  private static final Logger logger = Logger.getLogger(EchoClient.class.getName());

  public static void main(String[] args) {
    NameResolverRegistry.getDefaultRegistry()
        .register(
            new MultiAddressNameResolverProvider(
                new InetSocketAddress("localhost", 50000),
                new InetSocketAddress("localhost", 50001),
                new InetSocketAddress("localhost", 50002)));
    ManagedChannel channel =
        ManagedChannelBuilder.forTarget("service")
            .defaultLoadBalancingPolicy("round_robin")
            .usePlaintext()
            .build();

    EchoServiceGrpc.EchoServiceBlockingStub stub = EchoServiceGrpc.newBlockingStub(channel);
    for (int i = 0; i < 10; i++) {
      Echo.EchoResponse response =
          stub.echo(Echo.EchoRequest.newBuilder().setMessage("Hello " + i).build());
      logger.info(response.getMessage());
    }

    channel.shutdown();
  }
}
