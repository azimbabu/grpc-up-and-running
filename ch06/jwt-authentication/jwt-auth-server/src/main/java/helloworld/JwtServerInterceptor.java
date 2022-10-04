package helloworld;

import io.grpc.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

/**
 * https://github.com/grpc/grpc-java/blob/master/examples/example-jwt-auth/src/main/java/io/grpc/examples/jwtauth/JwtServerInterceptor.java
 */
public class JwtServerInterceptor implements ServerInterceptor {
  private final JwtParser jwtParser =
      Jwts.parserBuilder()
          .setSigningKey(Keys.hmacShaKeyFor(Constant.JWT_SIGNING_KEY.getBytes()))
          .build();

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    String value = headers.get(Constant.AUTHORIZATION_METADATA_KEY);
    Status status = Status.OK;
    if (value == null) {
      status = Status.UNAUTHENTICATED.withDescription("Authorization token is missing");
    } else if (!value.startsWith(Constant.BEARER_TYPE)) {
      status = Status.UNAUTHENTICATED.withDescription("Unknown authorization type");
    } else {
      // remove authorization type prefix
      String token = value.substring(Constant.BEARER_TYPE.length()).trim();
      try {
        // verify token signature and parse claims
        Jws<Claims> claims = jwtParser.parseClaimsJws(token);
        if (claims != null) {
          // set client id into current context
          Context context =
              Context.current()
                  .withValue(Constant.CLIENT_ID_CONTEXT_KEY, claims.getBody().getSubject());
          return Contexts.interceptCall(context, call, headers, next);
        }
      } catch (JwtException e) {
        status = Status.UNAUTHENTICATED.withDescription(e.getMessage()).withCause(e);
      }
    }

    call.close(status, new Metadata());
    return new ServerCall.Listener<ReqT>() {}; // noop
  }
}
