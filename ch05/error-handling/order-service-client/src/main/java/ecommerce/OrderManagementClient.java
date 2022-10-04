package ecommerce;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class OrderManagementClient {
  private static final Logger logger = Logger.getLogger(OrderManagementClient.class.getName());
  private static final int PORT = 50051;

  public static void main(String[] args) {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", PORT).usePlaintext().build();

    OrderManagementGrpc.OrderManagementBlockingStub stub =
        OrderManagementGrpc.newBlockingStub(channel).withDeadlineAfter(1000, TimeUnit.MILLISECONDS);

    // Creating an order with invalid Order ID.
    OrderManagementOuterClass.Order order1 =
        OrderManagementOuterClass.Order.newBuilder()
            .setId("-1")
            .addItems("iPhone XS")
            .addItems("Mac Book Pro")
            .setDestination("San Jose, CA")
            .setPrice(2300)
            .build();

    OrderManagementOuterClass.Order order2 =
        OrderManagementOuterClass.Order.newBuilder()
            .setId("1001")
            .addItems("iPhone XS")
            .addItems("Mac Book Pro")
            .setDestination("San Jose, CA")
            .setPrice(2300)
            .build();

    addOrder(stub, order1);
    addOrder(stub, order2);

    channel.shutdown();
  }

  private static void addOrder(
      OrderManagementGrpc.OrderManagementBlockingStub stub, OrderManagementOuterClass.Order order) {
    try {
      // Add Order with a deadline
      StringValue result = stub.addOrder(order);
      logger.info("Add order response, id=" + result.getValue());
    } catch (StatusRuntimeException e) {
      logger.info(
          "Error received from Server, code="
              + e.getStatus().getCode()
              + ", description="
              + e.getStatus().getDescription());
      logger.info("Error details : " + e.getMessage());
    }
  }
}
