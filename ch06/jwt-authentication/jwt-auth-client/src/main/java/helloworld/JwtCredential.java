package helloworld;

import java.util.concurrent.Executor;

import javax.crypto.SecretKey;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtCredential extends CallCredentials {
  private final String subject;

  public JwtCredential(String subject) {
    this.subject = subject;
  }

  @Override
  public void applyRequestMetadata(
      RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
    // Make a JWT compact serialized string.
    // This example omits setting the expiration, but a real application should do it.
    SecretKey key = Keys.hmacShaKeyFor(Constant.JWT_SIGNING_KEY.getBytes());
    String jwt = Jwts.builder().setSubject(subject).signWith(key).compact();

    appExecutor.execute(
        () -> {
          try {
            Metadata headers = new Metadata();
            headers.put(
                Constant.AUTHORIZATION_METADATA_KEY,
                String.format("%s %s", Constant.BEARER_TYPE, jwt));
            applier.apply(headers);
          } catch (Throwable e) {
            applier.fail(Status.UNAUTHENTICATED.withCause(e));
          }
        });
  }

  @Override
  public void thisUsesUnstableApi() {}
}
