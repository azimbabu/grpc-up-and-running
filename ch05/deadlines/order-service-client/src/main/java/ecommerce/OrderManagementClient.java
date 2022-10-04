package ecommerce;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class OrderManagementClient {
  private static final Logger logger = Logger.getLogger(OrderManagementClient.class.getName());
  private static final int PORT = 50051;

  public static void main(String[] args) throws InterruptedException {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", PORT).usePlaintext().build();

    addOrder(channel);
    addOrder(channel);
    addOrder(channel);
    addOrder(channel);
    addOrder(channel);

    channel.shutdown();
  }

  private static void addOrder(ManagedChannel channel) throws InterruptedException {
    Thread.sleep(5000L);

    OrderManagementGrpc.OrderManagementBlockingStub stub =
        OrderManagementGrpc.newBlockingStub(channel).withDeadlineAfter(1000, TimeUnit.MILLISECONDS);

    OrderManagementOuterClass.Order order =
        OrderManagementOuterClass.Order.newBuilder()
            .setId("" + ThreadLocalRandom.current().nextInt(1000))
            .addItems("iPhone XS")
            .addItems("Mac Book Pro")
            .setDestination("San Jose, CA")
            .setPrice(2300)
            .build();

    try {
      // Add order with a deadline
      stub.addOrder(order);
    } catch (StatusRuntimeException e) {
      if (Status.Code.DEADLINE_EXCEEDED == e.getStatus().getCode()) {
        logger.info("Deadline exceeded: " + e.getMessage());
      } else {
        logger.info(
            "Error from the service, code="
                + e.getStatus().getCode()
                + ", error="
                + e.getMessage());
      }
    }
  }
}
