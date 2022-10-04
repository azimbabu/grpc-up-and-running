package ecommerce;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;

public class OrderManagementClient {
  private static final Logger logger = Logger.getLogger(OrderManagementClient.class.getName());
  private static final int PORT = 50051;

  public static void main(String[] args) {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", PORT).usePlaintext().build();

    OrderManagementGrpc.OrderManagementBlockingStub orderManagementStub =
        OrderManagementGrpc.newBlockingStub(channel).withDeadlineAfter(1000, TimeUnit.MILLISECONDS);

    GreeterGrpc.GreeterBlockingStub greeterStub =
        GreeterGrpc.newBlockingStub(channel).withDeadlineAfter(1000, TimeUnit.MILLISECONDS);

    // Calling the Order Management gRPC service
    OrderManagementOuterClass.Order order =
        OrderManagementOuterClass.Order.newBuilder()
            .setId("1001")
            .addItems("iPhone XS")
            .addItems("Mac Book Pro")
            .setDestination("San Jose, CA")
            .setPrice(2300)
            .build();
    addOrder(orderManagementStub, order);

    // Calling the Greeter gRPC service
    sayHello(greeterStub, "gRPC Up and Running!");

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

  private static void sayHello(GreeterGrpc.GreeterBlockingStub stub, String name) {
    try {
      // Say hello RPC
      HelloRequest request = HelloRequest.newBuilder().setName(name).build();

      logger.info("[sayHello] Request from Client: " + request.getName());
      HelloReply reply = stub.sayHello(request);
      logger.info("[sayHello] Response from Server: " + reply.getMessage());
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
