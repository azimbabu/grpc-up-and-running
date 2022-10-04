package ecommerce;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class OrderManagementClient {
  private static final Logger logger = Logger.getLogger(OrderManagementClient.class.getName());

  public static void main(String[] args) {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 50051)
            .usePlaintext()
            .intercept(new OrderClientInterceptor())
            .build();

    OrderManagementGrpc.OrderManagementBlockingStub stub =
        OrderManagementGrpc.newBlockingStub(channel).withDeadlineAfter(1000, TimeUnit.MILLISECONDS);

    // Creating an order with invalid order id
    OrderManagementOuterClass.Order order =
        OrderManagementOuterClass.Order.newBuilder()
            .setId("101")
            .addItems("iPhone XS")
            .addItems("Macbook Pro")
            .setDestination("San Jose, CA")
            .setPrice(2300)
            .build();

    StringValue response = stub.addOrder(order);
    logger.info("AddOrder response: " + response.getValue());

    channel.shutdown();
  }
}
