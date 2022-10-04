package echo;

import java.util.logging.Logger;

import io.grpc.stub.StreamObserver;

public class EchoService extends EchoServiceGrpc.EchoServiceImplBase {
  private static final Logger logger = Logger.getLogger(EchoService.class.getName());

  private final String name;

  public EchoService(String name) {
    this.name = name;
  }

  @Override
  public void echo(Echo.EchoRequest request, StreamObserver<Echo.EchoResponse> responseObserver) {
    String message = request.getMessage();
    String reply = name + " echo: " + message;
    Echo.EchoResponse response = Echo.EchoResponse.newBuilder().setMessage(reply).build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
