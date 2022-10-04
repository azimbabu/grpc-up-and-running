package ecommerce;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.protobuf.StringValue;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class OrderManagementClient {
  private static final Logger logger = Logger.getLogger(OrderManagementClient.class.getName());
  private static final int PORT = 50051;

  public static void main(String[] args) {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", PORT).usePlaintext().build();

    OrderManagementGrpc.OrderManagementStub asyncStub = OrderManagementGrpc.newStub(channel);

    // Process Orders
    invokeProcessOrders(asyncStub);

    channel.shutdown();
  }

  private static void invokeProcessOrders(OrderManagementGrpc.OrderManagementStub asyncStub) {
    final CountDownLatch finishLatch = new CountDownLatch(1);
    Context.CancellableContext cancellableContext = Context.current().withCancellation();

    StreamObserver<OrderManagementOuterClass.CombinedShipment> processOrdersResponseObserver =
        new StreamObserver<>() {

          @Override
          public void onNext(OrderManagementOuterClass.CombinedShipment value) {
            logger.info(
                "Combined shipment, id=" + value.getId() + ", orders=" + value.getOrdersList());
          }

          @Override
          public void onError(Throwable t) {
            logger.warning("Error in processing orders, error=" + t.getMessage());
            t.printStackTrace();
          }

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

    try {
      Thread.sleep(1000);
      cancellableContext.cancel(null);
      logger.info("cancellableContext.isCancelled()=" + cancellableContext.isCancelled());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    if (cancellableContext.isCancelled()) {
      return;
    }

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
