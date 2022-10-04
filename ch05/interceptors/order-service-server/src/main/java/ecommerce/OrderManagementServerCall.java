package ecommerce;

import java.util.logging.Logger;

import io.grpc.ForwardingServerCall;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;

public class OrderManagementServerCall<ReqT, RespT>
    extends ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT> {
  private static final Logger logger = Logger.getLogger(OrderManagementServerCall.class.getName());

  protected OrderManagementServerCall(ServerCall<ReqT, RespT> delegate) {
    super(delegate);
  }

  @Override
  protected ServerCall<ReqT, RespT> delegate() {
    return super.delegate();
  }

  @Override
  public MethodDescriptor<ReqT, RespT> getMethodDescriptor() {
    return super.getMethodDescriptor();
  }

  @Override
  public void sendMessage(RespT message) {
    logger.info("Message from Server to Client: " + message);
    super.sendMessage(message);
  }
}
