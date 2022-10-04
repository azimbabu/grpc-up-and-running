package ecommerce;

import java.util.logging.Logger;

import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;

public class GreeterServiceImpl extends GreeterGrpc.GreeterImplBase {
  private static final Logger logger = Logger.getLogger(GreeterServiceImpl.class.getName());

  @Override
  public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
    logger.info("[GreeterService] sayHello received request from " + request.getName());
    HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + request.getName()).build();
    responseObserver.onNext(reply);
    responseObserver.onCompleted();
    logger.info("[GreeterService] sayHello returns response " + reply.getMessage());
  }
}
