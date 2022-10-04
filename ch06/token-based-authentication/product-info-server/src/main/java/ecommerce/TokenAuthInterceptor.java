package ecommerce;

import java.util.logging.Logger;

import io.grpc.*;

public class TokenAuthInterceptor implements ServerInterceptor {
  private static final Logger logger = Logger.getLogger(TokenAuthInterceptor.class.getName());

  private static final ServerCall.Listener NOOP_LISTENER = new ServerCall.Listener() {};
  private static final Context.Key<String> USER_ID_CONTEXT_KEY = Context.key("userId");
  private static final String ADMIN_USER_ID = "admin";
  private static final String ADMIN_USER_TOKEN = "some-secret-token";

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    String tokenString =
        headers.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));
    if (tokenString == null) {
      call.close(
          Status.UNAUTHENTICATED.withDescription("Token value is missing in Metadata"), headers);
      return NOOP_LISTENER;
    }

    if (validUser(tokenString)) {
      Context context = Context.current().withValue(USER_ID_CONTEXT_KEY, ADMIN_USER_ID);
      return Contexts.interceptCall(context, call, headers, next);
    } else {
      call.close(Status.UNAUTHENTICATED.withDescription("Invalid user token"), headers);
      return NOOP_LISTENER;
    }
  }

  private boolean validUser(String tokenString) {
    if (tokenString == null) {
      return false;
    }

    // Perform the token validation here. For the sake of this example, the code
    // here forgoes any of the usual OAuth2 token validation and instead checks
    // for a token matching an arbitrary string.
    String token = tokenString.substring("Bearer ".length()).trim();
    return ADMIN_USER_TOKEN.equals(token);
  }
}
