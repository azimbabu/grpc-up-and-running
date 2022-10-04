package helloworld;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * Load testing from command line :
 * ghz --insecure --proto ./helloworld.proto --call helloworld.Greeter.sayHello -d '{"name":"Joe"}' -n 2000 -c 20 localhost:50051
 */
public class HelloWorldServer
{
	private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());
	public static final int PORT = 50051;

	private Server server;

	private void start() throws IOException
	{
		server = ServerBuilder.forPort(PORT)
				.addService(new GreeterImpl())
				.build()
				.start();
		logger.info("Server started, listening on " + PORT);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			// Use stderr here since the logger may have been reset by its JVM shutdown hook.
			System.err.println("*** shutting down gRPC server since JVM is shutting down");
			try
			{
				HelloWorldServer.this.stop();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace(System.err);
			}
			System.err.println("*** server shut down");
		}));
	}

	private void stop() throws InterruptedException
	{
		if (server != null) {
			server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
		}
	}

	private void blockUntilShutdown() throws InterruptedException
	{
		if (server != null) {
			server.awaitTermination();
		}
	}

  public static void main(String[] args) throws IOException, InterruptedException
  {
	  final HelloWorldServer helloWorldServer = new HelloWorldServer();
	  helloWorldServer.start();
	  helloWorldServer.blockUntilShutdown();
  }
}
