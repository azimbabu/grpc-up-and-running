package ecommerce;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;

public class OrderManagementServiceImpl extends OrderManagementGrpc.OrderManagementImplBase {
  private static final Logger logger = Logger.getLogger(OrderManagementServiceImpl.class.getName());
  private static final int BATCH_SIZE = 3;

  private OrderManagementOuterClass.Order order1 =
      OrderManagementOuterClass.Order.newBuilder()
          .setId("102")
          .addItems("Google Pixel 3A")
          .addItems("Mac Book Pro")
          .setDescription("Mountain View, CA")
          .setPrice(1800)
          .build();

  private OrderManagementOuterClass.Order order2 =
      OrderManagementOuterClass.Order.newBuilder()
          .setId("103")
          .addItems("Apple Watch S4")
          .setDestination("San Jose, CA")
          .setPrice(400)
          .build();

  private OrderManagementOuterClass.Order order3 =
      OrderManagementOuterClass.Order.newBuilder()
          .setId("104")
          .addItems("Google Home Mini")
          .addItems("Google Nest Hub")
          .setDestination("Mountain View, CA")
          .setPrice(400)
          .build();

  private OrderManagementOuterClass.Order order4 =
      OrderManagementOuterClass.Order.newBuilder()
          .setId("105")
          .addItems("Amazon Echo")
          .setDestination("San Jose, CA")
          .setPrice(30)
          .build();

  private OrderManagementOuterClass.Order order5 =
      OrderManagementOuterClass.Order.newBuilder()
          .setId("106")
          .addItems("Amazon Echo")
          .addItems("Apple iPhone XS")
          .setDestination("Mountain View, CA")
          .setPrice(300)
          .build();

  private Map<String, OrderManagementOuterClass.Order> orderMap =
      Stream.of(
              new AbstractMap.SimpleEntry<>(order1.getId(), order1),
              new AbstractMap.SimpleEntry<>(order2.getId(), order2),
              new AbstractMap.SimpleEntry<>(order3.getId(), order3),
              new AbstractMap.SimpleEntry<>(order4.getId(), order4),
              new AbstractMap.SimpleEntry<>(order5.getId(), order5))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

  // Unary
  @Override
  public void getOrder(
      StringValue request, StreamObserver<OrderManagementOuterClass.Order> responseObserver) {
    OrderManagementOuterClass.Order order = orderMap.get(request.getValue());
    if (order != null) {
      logger.info("Order retrieved with id=" + order.getId());
      responseObserver.onNext(order);
      responseObserver.onCompleted();
    } else {
      logger.info("Order by id=" + request.getValue() + "- Not found.");
      responseObserver.onCompleted();
    }
  }

  // Server Streaming
  @Override
  public void searchOrders(
      StringValue request, StreamObserver<OrderManagementOuterClass.Order> responseObserver) {
    for (Map.Entry<String, OrderManagementOuterClass.Order> orderEntry : orderMap.entrySet()) {
      OrderManagementOuterClass.Order order = orderEntry.getValue();
      int itemsCount = order.getItemsCount();
      for (int index = 0; index < itemsCount; index++) {
        String item = order.getItems(index);
        if (item.contains(request.getValue())) {
          logger.info("Item found " + item);
          responseObserver.onNext(order);
          break;
        }
      }
    }
    responseObserver.onCompleted();
  }

  // Client Streaming
  @Override
  public StreamObserver<OrderManagementOuterClass.Order> updateOrders(
      StreamObserver<StringValue> responseObserver) {
    return new StreamObserver<>() {
      private StringBuilder updatedOrderStrBuilder =
          new StringBuilder().append("Updated order ids : ");

      @Override
      public void onNext(OrderManagementOuterClass.Order value) {
        if (value != null) {
          orderMap.put(value.getId(), value);
          updatedOrderStrBuilder.append(value.getId()).append(", ");
          logger.info("Order with id=" + value.getId() + " updated");
        }
      }

      @Override
      public void onError(Throwable t) {
        logger.info("Order update error=" + t.getMessage());
      }

      @Override
      public void onCompleted() {
        logger.info("Update orders - completed");
        StringValue updatedOrders =
            StringValue.newBuilder().setValue(updatedOrderStrBuilder.toString()).build();
        responseObserver.onNext(updatedOrders);
        responseObserver.onCompleted();
      }
    };
  }

  // Bidirectional Streaming
  @Override
  public StreamObserver<StringValue> processOrders(
      StreamObserver<OrderManagementOuterClass.CombinedShipment> responseObserver) {
    Map<String, OrderManagementOuterClass.CombinedShipment> combinedShipmentMap = new HashMap<>();

    return new StreamObserver<>() {
      private int batchMarker = 0;

      @Override
      public void onNext(StringValue value) {
        logger.info("Processing order, id=" + value.getValue());
        OrderManagementOuterClass.Order currentOrder = orderMap.get(value.getValue());
        if (currentOrder == null) {
          logger.warning("No order found, id=" + value.getValue());
          return;
        }

        // Processing an order and increment batch marker
        batchMarker++;
        String destination = currentOrder.getDestination();
        OrderManagementOuterClass.CombinedShipment existingShipment =
            combinedShipmentMap.get(destination);
        if (existingShipment != null) {
          existingShipment =
              OrderManagementOuterClass.CombinedShipment.newBuilder(existingShipment)
                  .addOrders(currentOrder)
                  .build();
          combinedShipmentMap.put(destination, existingShipment);
        } else {
          OrderManagementOuterClass.CombinedShipment newShipment =
              OrderManagementOuterClass.CombinedShipment.newBuilder().build();
          newShipment =
              newShipment
                  .newBuilderForType()
                  .addOrders(currentOrder)
                  .setId("CMB-" + ThreadLocalRandom.current().nextInt(1000) + ":" + destination)
                  .setStatus("Processed!")
                  .build();
          combinedShipmentMap.put(destination, newShipment);
        }

        if (batchMarker == BATCH_SIZE) {
          // Order batch completed. Flush all existing shipments.
          sendBatch(responseObserver);
          // Reset batch marker
          batchMarker = 0;
          combinedShipmentMap.clear();
        }
      }

      @Override
      public void onError(Throwable t) {}

      @Override
      public void onCompleted() {
        sendBatch(responseObserver);
        responseObserver.onCompleted();
      }

      private void sendBatch(
          StreamObserver<OrderManagementOuterClass.CombinedShipment> responseObserver) {
        for (Map.Entry<String, OrderManagementOuterClass.CombinedShipment> entry :
            combinedShipmentMap.entrySet()) {
          responseObserver.onNext(entry.getValue());
        }
      }
    };
  }
}
