package ecommerce;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
      Arrays.asList(order1, order2, order3, order4, order5).stream()
          .collect(Collectors.toMap(OrderManagementOuterClass.Order::getId, Function.identity()));

  // Unary
  @Override
  public void addOrder(
      OrderManagementOuterClass.Order request, StreamObserver<StringValue> responseObserver) {
    logger.info(
        "AddOrder request arrived, id="
            + request.getId()
            + ", destination="
            + request.getDestination());
    orderMap.put(request.getId(), request);
    StringValue id =
        StringValue.newBuilder().setValue("" + ThreadLocalRandom.current().nextLong()).build();
    responseObserver.onNext(id);
    responseObserver.onCompleted();
    logger.info(
        "AddOrder request processed, id="
            + request.getId()
            + ", destination="
            + request.getDestination());
  }

  // Unary
  @Override
  public void getOrder(
      StringValue request, StreamObserver<OrderManagementOuterClass.Order> responseObserver) {
    OrderManagementOuterClass.Order order = orderMap.get(request.getValue());
    if (order != null) {
      logger.info("Order retrieved, id=" + order.getId());
      responseObserver.onNext(order);
      responseObserver.onCompleted();
    } else {
      logger.info("Order is not found, id=" + request.getValue());
      responseObserver.onCompleted();
    }
    // TODO Handle errors
    // responseObserver.onError();
  }

  // Server Streaming
  @Override
  public void searchOrders(
      StringValue request, StreamObserver<OrderManagementOuterClass.Order> responseObserver) {
    orderMap.values().stream()
        .filter(
            order ->
                order.getItemsList().stream()
                    .anyMatch(
                        item -> {
                          if (item.contains(request.getValue())) {
                            logger.info(
                                "Item found, orderId="
                                    + order.getId()
                                    + ", item="
                                    + item
                                    + ", searchKeyword="
                                    + request.getValue());
                            return true;
                          } else {
                            return false;
                          }
                        }))
        .forEach(order -> responseObserver.onNext(order));
    responseObserver.onCompleted();
  }

  // Client Streaming
  @Override
  public StreamObserver<OrderManagementOuterClass.Order> updateOrders(
      StreamObserver<StringValue> responseObserver) {
    return new StreamObserver<>() {
      private StringBuilder updatedOrderIdsBuilder =
          new StringBuilder().append("Updated order ids=");

      @Override
      public void onNext(OrderManagementOuterClass.Order value) {
        if (value != null) {
          orderMap.put(value.getId(), value);
          updatedOrderIdsBuilder.append(value.getId()).append(", ");
          logger.info("Order is updated, id=" + value.getId());
        }
      }

      @Override
      public void onError(Throwable t) {}

      @Override
      public void onCompleted() {
        logger.info("Update orders is completed");
        StringValue updatedOrderIds =
            StringValue.newBuilder().setValue(updatedOrderIdsBuilder.toString()).build();
        responseObserver.onNext(updatedOrderIds);
        responseObserver.onCompleted();
      }
    };
  }

  // Bi-di Streaming
  @Override
  public StreamObserver<StringValue> processOrders(
      StreamObserver<OrderManagementOuterClass.CombinedShipment> responseObserver) {
    final Map<String, OrderManagementOuterClass.CombinedShipment> combinedShipmentMap =
        new HashMap<>();

    return new StreamObserver<>() {
      private int batchMarker = 0;

      @Override
      public void onNext(StringValue value) {
        logger.info("Processing order, id=" + value.getValue());
        OrderManagementOuterClass.Order order = orderMap.get(value.getValue());
        if (order == null) {
          logger.info("Order is not found, id=" + value.getValue());
          return;
        }

        // Processing an order and increment batch maker to
        batchMarker++;
        OrderManagementOuterClass.CombinedShipment combinedShipment =
            combinedShipmentMap.get(order.getDestination());
        if (combinedShipment != null) {
          combinedShipment =
              OrderManagementOuterClass.CombinedShipment.newBuilder(combinedShipment)
                  .addOrders(order)
                  .build();
        } else {
          combinedShipment =
              OrderManagementOuterClass.CombinedShipment.newBuilder()
                  .setId(
                      "CMB-"
                          + ThreadLocalRandom.current().nextInt(1000)
                          + ":"
                          + order.getDestination())
                  .addOrders(order)
                  .setStatus("Processed")
                  .build();
        }
        combinedShipmentMap.put(order.getDestination(), combinedShipment);

        if (batchMarker == BATCH_SIZE) {
          // Order batch completed, flush existing shipments
          combinedShipmentMap.values().forEach(shipment -> responseObserver.onNext(shipment));
          // reset batch marker
          batchMarker = 0;
          combinedShipmentMap.clear();
        }
      }

      @Override
      public void onError(Throwable t) {}

      @Override
      public void onCompleted() {
        combinedShipmentMap.values().forEach(shipment -> responseObserver.onNext(shipment));
        responseObserver.onCompleted();
      }
    };
  }
}
