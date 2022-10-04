package ecommerce;

import java.util.logging.Logger;

import io.grpc.*;

public class OrderManagementClientInterceptor implements ClientInterceptor {
  private static final Logger logger =
      Logger.getLogger(OrderManagementClientInterceptor.class.getName());

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
      MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
    // Preprocessor phase
    logger.info("[Client Interceptor] Remote method invoked=" + method);

    // invoking the remote method
    ClientCall<ReqT, RespT> clientCall = next.newCall(method, callOptions);

    return new OrderManagementClientCall<>(clientCall);
  }
}
