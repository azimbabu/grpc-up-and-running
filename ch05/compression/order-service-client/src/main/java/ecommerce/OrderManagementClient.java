package ecommerce;

import java.util.logging.Logger;

import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class OrderManagementClient {
  private static final Logger logger = Logger.getLogger(OrderManagementClient.class.getName());

  public static void main(String[] args) {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();
    OrderManagementGrpc.OrderManagementBlockingStub stub =
        OrderManagementGrpc.newBlockingStub(channel).withCompression("gzip");

    OrderManagementOuterClass.Order order =
        OrderManagementOuterClass.Order.newBuilder()
            .setId("101")
            .addItems("iPhone XS")
            .addItems("MacBook Pro")
            .setDestination("San Jose, CA")
            .setPrice(2300)
            .build();

    // Add order
    StringValue addOrderResponse = stub.addOrder(order);
    logger.info("AddOrder response: " + addOrderResponse.getValue());

    // Get Order
    StringValue idValue = StringValue.newBuilder().setValue("101").build();
    OrderManagementOuterClass.Order getOrderResponse = stub.getOrder(idValue);
    logger.info("GetOrder response: " + getOrderResponse.toString());

    channel.shutdown();
  }
}
