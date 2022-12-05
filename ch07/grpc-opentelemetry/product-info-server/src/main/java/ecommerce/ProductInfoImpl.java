package ecommerce;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class ProductInfoImpl extends ProductInfoGrpc.ProductInfoImplBase {
    private static final Logger logger = Logger.getLogger(ProductInfoImpl.class.getName());

    private final Map<String, ProductInfoOuterClass.Product> productMap = new HashMap<>();

    @Override
    public void addProduct(
            ProductInfoOuterClass.Product request,
            StreamObserver<ProductInfoOuterClass.ProductID> responseObserver) {
        String id = UUID.randomUUID().toString();
        productMap.put(id, request);
        logger.info("Added product=" + request + ", id=" + id);

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
            ProductInfoOuterClass.Product product = productMap.get(id);
            logger.info("Found product=" + product + ", id=" + id);
            responseObserver.onNext(product);
            responseObserver.onCompleted();
        } else {
            logger.warning("Product not found, id=" + id);
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
        }
    }
}
