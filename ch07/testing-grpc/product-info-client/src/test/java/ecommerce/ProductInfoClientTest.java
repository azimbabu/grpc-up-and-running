package ecommerce;

import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class ProductInfoClientTest {
  @Rule public final GrpcCleanupRule grpcCleanupRule = new GrpcCleanupRule();

  private final ProductInfoGrpc.ProductInfoImplBase serviceImpl =
      Mockito.mock(
          ProductInfoGrpc.ProductInfoImplBase.class,
          AdditionalAnswers.delegatesTo(
              new ProductInfoGrpc.ProductInfoImplBase() {
                @Override
                public void addProduct(
                    ProductInfoOuterClass.Product request,
                    StreamObserver<ProductInfoOuterClass.ProductID> responseObserver) {
                  ProductInfoOuterClass.ProductID productID =
                      ProductInfoOuterClass.ProductID.newBuilder()
                          .setValue(UUID.randomUUID().toString())
                          .build();
                  responseObserver.onNext(productID);
                  responseObserver.onCompleted();
                }

                @Override
                public void getProduct(
                    ProductInfoOuterClass.ProductID request,
                    StreamObserver<ProductInfoOuterClass.Product> responseObserver) {
                  ProductInfoOuterClass.Product product =
                      ProductInfoOuterClass.Product.newBuilder()
                          .setId(request.getValue())
                          .setName("Apple iPhone 11")
                          .setDescription(
                              "Meet Apple iPhone 11. All-new dual-camera system with Ultra Wide and Night mode.")
                          .setPrice(1000.0f)
                          .build();
                  responseObserver.onNext(product);
                  responseObserver.onCompleted();
                }
              }));

  private ProductInfoClient productInfoClient;

  @Before
  public void setUp() throws Exception {
    // Generate a unique in-process server name.
    String serverName = InProcessServerBuilder.generateName();

    // Create a server, add service, start, and register for automatic graceful shutdown.
    grpcCleanupRule.register(
        InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(serviceImpl)
            .build()
            .start());

    // Create a client channel and register for automatic graceful shutdown.
    ManagedChannel channel =
        grpcCleanupRule.register(
            InProcessChannelBuilder.forName(serverName).directExecutor().build());

    // Create a ProductInfoClient using the in-process channel
    productInfoClient = new ProductInfoClient(channel);
  }

  @Test
  public void addProduct() {
    ArgumentCaptor<ProductInfoOuterClass.Product> requestCaptor =
        ArgumentCaptor.forClass(ProductInfoOuterClass.Product.class);

    ProductInfoOuterClass.Product product =
        ProductInfoOuterClass.Product.newBuilder()
            .setName("Apple iPhone 11")
            .setDescription(
                "Meet Apple iPhone 11. All-new dual-camera system with Ultra Wide and Night mode.")
            .setPrice(1000.0f)
            .build();
    productInfoClient.addProduct(product);
    verify(serviceImpl).addProduct(requestCaptor.capture(), ArgumentMatchers.any());
    assertEquals(product, requestCaptor.getValue());
  }

  @Test
  public void getProduct() {
    ArgumentCaptor<ProductInfoOuterClass.ProductID> requestCaptor =
        ArgumentCaptor.forClass(ProductInfoOuterClass.ProductID.class);

    String id = UUID.randomUUID().toString();
    productInfoClient.getProduct(id);
    verify(serviceImpl).getProduct(requestCaptor.capture(), ArgumentMatchers.any());
    assertEquals(id, requestCaptor.getValue().getValue());
  }
}
