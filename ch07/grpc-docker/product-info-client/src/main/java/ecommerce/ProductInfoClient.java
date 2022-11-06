package ecommerce;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.logging.Logger;

public class ProductInfoClient {

  private static final Logger logger = Logger.getLogger(ProductInfoClient.class.getName());

  private final ProductInfoGrpc.ProductInfoBlockingStub blockingStub;

  public ProductInfoClient(ManagedChannel channel) {
    blockingStub = ProductInfoGrpc.newBlockingStub(channel);
  }

  public static void main(String[] args) {
    String hostName = System.getProperty("serverHostName", "localhost");
    int port = Integer.parseInt(System.getProperty("serverPort", "50051"));
    logger.info("hostname=" + hostName + ", port=" + port);

    ManagedChannel channel =
        ManagedChannelBuilder.forAddress(hostName, port).usePlaintext().build();
    ProductInfoClient productInfoClient = new ProductInfoClient(channel);

    try {
      ProductInfoOuterClass.Product product =
          ProductInfoOuterClass.Product.newBuilder()
              .setName("Apple iPhone 11")
              .setDescription(
                  "Meet Apple iPhone 11. All-new dual-camera system with Ultra Wide and Night mode.")
              .setPrice(1000.0f)
              .build();
      String id = productInfoClient.addProduct(product);
      logger.info("Product added, id: " + id);

      ProductInfoOuterClass.Product productFound = productInfoClient.getProduct(id);
      logger.info("Product found: " + productFound);
    } finally {
      channel.shutdown();
    }
  }

  public String addProduct(ProductInfoOuterClass.Product product) {
    ProductInfoOuterClass.ProductID productID = blockingStub.addProduct(product);
    return productID.getValue();
  }

  public ProductInfoOuterClass.Product getProduct(String id) {
    ProductInfoOuterClass.ProductID productID =
        ProductInfoOuterClass.ProductID.newBuilder().setValue(id).build();
    return blockingStub.getProduct(productID);
  }
}
