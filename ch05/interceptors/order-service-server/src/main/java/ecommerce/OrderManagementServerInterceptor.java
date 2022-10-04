package ecommerce;

import java.util.logging.Logger;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

public class OrderManagementServerInterceptor implements ServerInterceptor {
  private static final Logger logger =
      Logger.getLogger(OrderManagementServerInterceptor.class.getName());

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    logger.info(
        "[Server Interceptor], Remote method invoked - "
            + call.getMethodDescriptor().getFullMethodName());
    ServerCall<ReqT, RespT> serverCall = new OrderManagementServerCall<>(call);
    return new OrderManagementServerCallListener<>(next.startCall(serverCall, headers));
  }
}
