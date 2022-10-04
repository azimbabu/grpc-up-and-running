package ecommerce;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.protobuf.StringValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class OrderManagementClient {
  private static final Logger logger = Logger.getLogger(OrderManagementClient.class.getName());

  public static void main(String[] args) {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();
    OrderManagementGrpc.OrderManagementBlockingStub stub =
        OrderManagementGrpc.newBlockingStub(channel);
    OrderManagementGrpc.OrderManagementStub asyncStub = OrderManagementGrpc.newStub(channel);

    // Get Order
    getOrder(stub);

    // Search Orders
    searchOrders(stub);

    // Update orders
    updateOrders(asyncStub);

    // Process orders
    processOrders(asyncStub);

    channel.shutdown();
  }

  private static void processOrders(OrderManagementGrpc.OrderManagementStub asyncStub) {
    final CountDownLatch finishLatch = new CountDownLatch(1);

    StreamObserver<OrderManagementOuterClass.CombinedShipment> orderProcessResponseObserver =
        new StreamObserver<>() {
          @Override
          public void onNext(OrderManagementOuterClass.CombinedShipment value) {
            logger.info("Combined shipment=" + value);
          }

          @Override
          public void onError(Throwable t) {}

          @Override
          public void onCompleted() {
            logger.info("Order processing completed!");
            finishLatch.countDown();
          }
        };

    StreamObserver<StringValue> orderProcessRequestObserver =
        asyncStub.processOrders(orderProcessResponseObserver);
    orderProcessRequestObserver.onNext(StringValue.newBuilder().setValue("102").build());
    orderProcessRequestObserver.onNext(StringValue.newBuilder().setValue("103").build());
    orderProcessRequestObserver.onNext(StringValue.newBuilder().setValue("104").build());
    orderProcessRequestObserver.onNext(StringValue.newBuilder().setValue("101").build());

    if (finishLatch.getCount() == 0) {
      logger.warning("RPC completed or errored before we finished sending.");
      return;
    }

    orderProcessRequestObserver.onCompleted();

    try {
      if (!finishLatch.await(60, TimeUnit.SECONDS)) {
        logger.warning("FAILED : Process orders cannot finish within 60 seconds");
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static void updateOrders(OrderManagementGrpc.OrderManagementStub asyncStub) {
    OrderManagementOuterClass.Order orderUpdate1 =
        OrderManagementOuterClass.Order.newBuilder()
            .setId("102")
            .addItems("Google Pixel 3A")
            .addItems("Google Pixel Book")
            .setDestination("Mountain View, CA")
            .setPrice(1100)
            .build();

    OrderManagementOuterClass.Order orderUpdate2 =
        OrderManagementOuterClass.Order.newBuilder()
            .setId("103")
            .addItems("Apple Watch S4")
            .addItems("Mac Book Pro")
            .addItems("iPad Pro")
            .setDestination("San Jose, CA")
            .setPrice(2800)
            .build();

    OrderManagementOuterClass.Order orderUpdate3 =
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
            logger.info("Update Orders response=" + value.getValue());
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
    updateOrderRequestObserver.onNext(orderUpdate1);
    updateOrderRequestObserver.onNext(orderUpdate2);
    updateOrderRequestObserver.onNext(orderUpdate3);
    updateOrderRequestObserver.onNext(orderUpdate3);

    if (finishLatch.getCount() == 0) {
      logger.warning("RPC completed or errored before we finished sending");
      return;
    }

    updateOrderRequestObserver.onCompleted();

    // Receiving happens asynchronously
    try {
      if (!finishLatch.await(10, TimeUnit.SECONDS)) {
        logger.warning("FAILED : Process orders cannot finish within 10 seconds");
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static void searchOrders(OrderManagementGrpc.OrderManagementBlockingStub stub) {
    StringValue searchStr = StringValue.newBuilder().setValue("Google").build();
    Iterator<OrderManagementOuterClass.Order> matchingOrderIterator = stub.searchOrders(searchStr);
    while (matchingOrderIterator.hasNext()) {
      OrderManagementOuterClass.Order matchingOrder = matchingOrderIterator.next();
      logger.info("Search order response, matching order=" + matchingOrder);
    }
  }

  private static void getOrder(OrderManagementGrpc.OrderManagementBlockingStub stub) {
    StringValue id = StringValue.newBuilder().setValue("102").build();
    OrderManagementOuterClass.Order order = stub.getOrder(id);
    logger.info("GetOrder response=" + order);
  }
}
