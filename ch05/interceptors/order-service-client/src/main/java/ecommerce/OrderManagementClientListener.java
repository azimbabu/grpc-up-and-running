package ecommerce;

import java.util.logging.Logger;

import io.grpc.ClientCall;
import io.grpc.ForwardingClientCallListener;

public class OrderManagementClientListener<RespT>
    extends ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT> {
  private static final Logger logger =
      Logger.getLogger(OrderManagementClientListener.class.getName());

  protected OrderManagementClientListener(ClientCall.Listener<RespT> delegate) {
    super(delegate);
  }

  @Override
  public void onMessage(RespT message) {
    logger.info("Message from Server to Client: " + message);
    super.onMessage(message);
  }
}
