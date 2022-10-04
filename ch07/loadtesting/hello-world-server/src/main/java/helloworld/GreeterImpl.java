package helloworld;

import java.util.logging.Logger;

import io.grpc.stub.StreamObserver;

public class GreeterImpl extends GreeterGrpc.GreeterImplBase
{
	private static final Logger logger = Logger.getLogger(GreeterImpl.class.getName());

	@Override
	public void sayHello(Helloworld.HelloRequest request,
			StreamObserver<Helloworld.HelloReply> responseObserver)
	{
		logger.info("Request=" + request);
		Helloworld.HelloReply reply = Helloworld.HelloReply.newBuilder().setMessage("Hello " + request.getName()).build();
		logger.info("Response=" + reply);
		responseObserver.onNext(reply);
		responseObserver.onCompleted();
	}
}
