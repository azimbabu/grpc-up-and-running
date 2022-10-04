package ecommerce;

import java.util.logging.Logger;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

public class MetadataInterceptor implements ServerInterceptor {
  private static final Logger logger = Logger.getLogger(MetadataInterceptor.class.getName());

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {
    String metadataValue =
        metadata.get(Metadata.Key.of("MY_MD_1", Metadata.ASCII_STRING_MARSHALLER));
    logger.info("Metadata retrieved: " + metadataValue);
    return next.startCall(call, metadata);
  }
}
