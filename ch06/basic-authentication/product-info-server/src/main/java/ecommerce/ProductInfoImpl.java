package ecommerce;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;

public class ProductInfoImpl extends ProductInfoGrpc.ProductInfoImplBase {
  private static final Logger logger = Logger.getLogger(ProductInfoImpl.class.getName());

  private final Map<String, ProductInfoOuterClass.Product> productMap = new HashMap<>();

  @Override
  public void addProduct(
      ProductInfoOuterClass.Product request,
      StreamObserver<ProductInfoOuterClass.ProductID> responseObserver) {
    ProductInfoOuterClass.Product product =
        request.toBuilder().setId(UUID.randomUUID().toString()).build();
    productMap.put(product.getId(), product);
    ProductInfoOuterClass.ProductID productID =
        ProductInfoOuterClass.ProductID.newBuilder().setValue(product.getId()).build();
    responseObserver.onNext(productID);
    responseObserver.onCompleted();
  }

  @Override
  public void getProduct(
      ProductInfoOuterClass.ProductID request,
      StreamObserver<ProductInfoOuterClass.Product> responseObserver) {
    String id = request.getValue();
    if (productMap.containsKey(id)) {
      responseObserver.onNext(productMap.get(id));
      responseObserver.onCompleted();
    } else {
      responseObserver.onError(new StatusException(Status.NOT_FOUND));
    }
  }
}
