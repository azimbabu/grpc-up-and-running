package ecommerce;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.protobuf.StringValue;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class OrderManagementClient {
  private static final Logger logger = Logger.getLogger(OrderManagementClient.class.getName());

  public static void main(String[] args) {
    ManagedChannel managedChannel =
        ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();

    Channel channel =
        ClientInterceptors.intercept(managedChannel, new OrderManagementClientInterceptor());

    OrderManagementGrpc.OrderManagementBlockingStub stub =
        OrderManagementGrpc.newBlockingStub(channel);

    OrderManagementGrpc.OrderManagementStub asyncStub = OrderManagementGrpc.newStub(channel);

    OrderManagementOuterClass.Order order =
        OrderManagementOuterClass.Order.newBuilder()
            .setId("101")
            .addItems("iPhone XS")
            .addItems("Max Book Pro")
            .setDestination("San Jose, CA")
            .setPrice(2300)
            .build();

    // Add order
    StringValue addOrderResponse = stub.addOrder(order);
    logger.info("Add order response=" + addOrderResponse.getValue());

    // Get Order
    StringValue id = StringValue.newBuilder().setValue("101").build();
    OrderManagementOuterClass.Order getOrderResponse = stub.getOrder(id);
    logger.info("Get order response=" + getOrderResponse.toString());

    // Search Orders
    StringValue searchKeywords = StringValue.newBuilder().setValue("Google").build();
    Iterator<OrderManagementOuterClass.Order> searchOrdersIterator =
        stub.searchOrders(searchKeywords);
    while (searchOrdersIterator.hasNext()) {
      OrderManagementOuterClass.Order orderFound = searchOrdersIterator.next();
      logger.info("Search orders response, matching order=" + orderFound.toString());
    }

    // Update Orders
    invokeOrderUpdate(asyncStub);

    // Process Orders
    invokeProcessOrders(asyncStub);

    managedChannel.shutdown();
  }

  private static void invokeOrderUpdate(OrderManagementGrpc.OrderManagementStub asyncStub) {
    OrderManagementOuterClass.Order order1 =
        OrderManagementOuterClass.Order.newBuilder()
            .setId("102")
            .addItems("Google Pixel 3A")
            .addItems("Google Pixel Book")
            .setDestination("Mountain View, CA")
            .setPrice(1100)
            .build();

    OrderManagementOuterClass.Order order2 =
        OrderManagementOuterClass.Order.newBuilder()
            .setId("103")
            .addItems("Apple Watch S4")
            .addItems("Mac Book Pro")
            .addItems("iPad Pro")
            .setDestination("San Jose, CA")
            .setPrice(2800)
            .build();

    OrderManagementOuterClass.Order order3 =
        OrderManagementOuterClass.Order.newBuilder()
            .setId("104")
            .addItems("Google Home Mini")
            .addItems("Google Nest Hub")
            .addItems("iPad Mini")
            .setDestination("Mountain View, CA")
            .setPrice(2200)
            .build();

    final CountDownLatch finishLatch = new CountDownLatch(1);

    StreamObserver<StringValue> updateOrderResponseObserver =
        new StreamObserver<>() {
          @Override
          public void onNext(StringValue value) {
            logger.info("Update orders response=" + value.getValue());
          }

          @Override
          public void onError(Throwable t) {}

          @Override
          public void onCompleted() {
            logger.info("Update orders response completed!");
            finishLatch.countDown();
          }
        };

    StreamObserver<OrderManagementOuterClass.Order> updateOrderRequestObserver =
        asyncStub.updateOrders(updateOrderResponseObserver);
    updateOrderRequestObserver.onNext(order1);
    updateOrderRequestObserver.onNext(order2);
    updateOrderRequestObserver.onNext(order3);

    if (finishLatch.getCount() == 0) {
      logger.warning("RPC completed or errored before we finished sending");
      return;
    }
    updateOrderRequestObserver.onCompleted();

    // Receiving happens asynchronously
    try {
      if (!finishLatch.await(10, TimeUnit.SECONDS)) {
        logger.warning("FAILED: Update Orders didn't finish within 10 seconds");
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static void invokeProcessOrders(OrderManagementGrpc.OrderManagementStub asyncStub) {
    final CountDownLatch finishLatch = new CountDownLatch(1);

    StreamObserver<OrderManagementOuterClass.CombinedShipment> processOrdersResponseObserver =
        new StreamObserver<>() {

          @Override
          public void onNext(OrderManagementOuterClass.CombinedShipment value) {
            logger.info(
                "Combined shipment, id=" + value.getId() + ", orders=" + value.getOrdersList());
          }

          @Override
          public void onError(Throwable t) {}

          @Override
          public void onCompleted() {
            logger.info("Process orders is completed!");
            finishLatch.countDown();
          }
        };

    StreamObserver<StringValue> processOrdersRequestObserver =
        asyncStub.processOrders(processOrdersResponseObserver);
    processOrdersRequestObserver.onNext(StringValue.newBuilder().setValue("102").build());
    processOrdersRequestObserver.onNext(StringValue.newBuilder().setValue("103").build());
    processOrdersRequestObserver.onNext(StringValue.newBuilder().setValue("104").build());
    processOrdersRequestObserver.onNext(StringValue.newBuilder().setValue("101").build());

    if (finishLatch.getCount() == 0) {
      logger.warning("RPC completed or errored before we finished sending.");
      return;
    }
    processOrdersRequestObserver.onCompleted();

    try {
      if (!finishLatch.await(120, TimeUnit.SECONDS)) {
        logger.warning("FAILED: Process orders cannot finish within 120 seconds");
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
