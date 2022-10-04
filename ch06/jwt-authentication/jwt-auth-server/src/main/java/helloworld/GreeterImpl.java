package helloworld;

import java.util.logging.Logger;

import io.grpc.stub.StreamObserver;

public class GreeterImpl extends GreeterGrpc.GreeterImplBase {
  private static final Logger logger = Logger.getLogger(GreeterImpl.class.getName());

  @Override
  public void sayHello(
      Helloworld.HelloRequest request, StreamObserver<Helloworld.HelloReply> responseObserver) {
    // get client id added to context by interceptor
    String clientId = Constant.CLIENT_ID_CONTEXT_KEY.get();
    logger.info("Processing request from " + clientId);
    Helloworld.HelloReply reply =
        Helloworld.HelloReply.newBuilder().setMessage("Hello, " + request.getName()).build();
    responseObserver.onNext(reply);
    responseObserver.onCompleted();
  }
}
