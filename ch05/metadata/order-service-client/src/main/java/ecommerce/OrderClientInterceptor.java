package ecommerce;

import io.grpc.*;

public class OrderClientInterceptor implements ClientInterceptor {
  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
      MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
    return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
        next.newCall(method, callOptions)) {
      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        headers.put(
            Metadata.Key.of("MY_MD_1", Metadata.ASCII_STRING_MARSHALLER),
            "This is metadata of MY_MD_1");
        super.start(responseListener, headers);
      }
    };
  }
}
