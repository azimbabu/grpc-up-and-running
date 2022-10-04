package ecommerce;

import java.util.logging.Logger;

import io.grpc.ClientCall;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;

public class OrderManagementClientCall<ReqT, RespT>
    extends ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT> {
  private static final Logger logger = Logger.getLogger(OrderManagementClientCall.class.getName());

  protected OrderManagementClientCall(ClientCall<ReqT, RespT> delegate) {
    super(delegate);
  }

  @Override
  public void start(Listener<RespT> responseListener, Metadata headers) {
    super.start(new OrderManagementClientListener<>(responseListener), headers);
  }

  @Override
  public void sendMessage(ReqT message) {
    logger.info("Message from Client to Server: " + message);
    super.sendMessage(message);
  }
}
