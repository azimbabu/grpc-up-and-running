package ecommerce;

import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ProductInfoClient {
  private static final Logger logger = Logger.getLogger(ProductInfoClient.class.getName());

  public static void main(String[] args) {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();

    ProductInfoGrpc.ProductInfoBlockingStub stub = ProductInfoGrpc.newBlockingStub(channel);

    ProductInfoOuterClass.ProductID productID =
        stub.addProduct(
            ProductInfoOuterClass.Product.newBuilder()
                .setName("Apple iPhone 11")
                .setDescription(
                    "Meet Apple iPhone 11. All-new dual-camera system with Ultra Wide and Night mode.")
                .setPrice(1000.0f)
                .build());
    logger.info("Product ID=" + productID.getValue() + " added successfully");

    ProductInfoOuterClass.Product product = stub.getProduct(productID);
    logger.info("Product=" + product);
    channel.shutdown();
  }
}
