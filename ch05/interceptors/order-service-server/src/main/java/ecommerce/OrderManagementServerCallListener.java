package ecommerce;

import java.util.logging.Logger;

import io.grpc.ForwardingServerCallListener;
import io.grpc.ServerCall;

public class OrderManagementServerCallListener<R> extends ForwardingServerCallListener<R> {
  private static final Logger logger =
      Logger.getLogger(OrderManagementServerCallListener.class.getName());

  private final ServerCall.Listener<R> delegate;

  public OrderManagementServerCallListener(ServerCall.Listener<R> delegate) {
    this.delegate = delegate;
  }

  @Override
  protected ServerCall.Listener<R> delegate() {
    return delegate;
  }

  @Override
  public void onMessage(R message) {
    logger.info("Message received from Client to Server: " + message);
    super.onMessage(message);
  }
}
