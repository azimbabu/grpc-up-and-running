package ecommerce;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProductInfoImpl extends ProductInfoGrpc.ProductInfoImplBase {

    private Map<String, ProductInfoOuterClass.Product> productMap = new HashMap<>();

    @Override
    public void addProduct(
            ProductInfoOuterClass.Product request,
            StreamObserver<ProductInfoOuterClass.ProductID> responseObserver) {
        String id = UUID.randomUUID().toString();
        productMap.put(id, request);

        ProductInfoOuterClass.ProductID productID =
                ProductInfoOuterClass.ProductID.newBuilder().setValue(id).build();
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
