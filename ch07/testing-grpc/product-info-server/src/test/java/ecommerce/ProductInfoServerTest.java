package ecommerce;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;

import static org.junit.Assert.*;

public class ProductInfoServerTest {
  /**
   * This rule manages automatic graceful shutdown for the registered servers and channels at the
   * end of test.
   */
  @Rule public final GrpcCleanupRule grpcCleanupRule = new GrpcCleanupRule();

  private ProductInfoGrpc.ProductInfoBlockingStub blockingStub;

  @Before
  public void setUp() throws Exception {
    // Generate a unique in-process server name.
    String serverName = InProcessServerBuilder.generateName();

    // Create a server, add service, start, and register for automatic graceful shutdown.
    grpcCleanupRule.register(
        InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(new ProductInfoImpl())
            .build()
            .start());

    blockingStub =
        ProductInfoGrpc.newBlockingStub(
            // Create a client channel and register for automatic graceful shutdown.
            grpcCleanupRule.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build()));
  }

  @Test
  public void testAddProduct() {
    ProductInfoOuterClass.Product product = buildProduct();
    ProductInfoOuterClass.ProductID productID = blockingStub.addProduct(product);
    assertNotNull(productID);
    assertNotNull(productID.getValue());
    assertFalse(productID.getValue().isEmpty());
  }

  private ProductInfoOuterClass.Product buildProduct() {
    ProductInfoOuterClass.Product product =
        ProductInfoOuterClass.Product.newBuilder()
            .setName("Apple iPhone 11")
            .setDescription(
                "Meet Apple iPhone 11. All-new dual-camera system with Ultra Wide and Night mode.")
            .setPrice(1000.0f)
            .build();
    return product;
  }

  @Test
  public void getProduct() {
    ProductInfoOuterClass.Product expectedProduct = buildProduct();
    ProductInfoOuterClass.ProductID productID = blockingStub.addProduct(expectedProduct);

    ProductInfoOuterClass.Product actualProduct = blockingStub.getProduct(productID);
    assertNotNull(actualProduct);
    assertEquals(expectedProduct.getId(), actualProduct.getId());
    assertEquals(expectedProduct.getName(), actualProduct.getName());
    assertEquals(expectedProduct.getDescription(), actualProduct.getDescription());
    assertEquals(expectedProduct.getPrice(), actualProduct.getPrice(), 0.00001);
  }
}
