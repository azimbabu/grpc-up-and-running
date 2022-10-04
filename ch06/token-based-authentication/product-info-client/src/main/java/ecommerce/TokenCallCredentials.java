package ecommerce;

import java.util.concurrent.Executor;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;

public class TokenCallCredentials extends CallCredentials {
  private final String credentials;

  public TokenCallCredentials(String token) {
    this.credentials = token;
  }

  @Override
  public void applyRequestMetadata(
      RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
    appExecutor.execute(
        () -> {
          try {
            Metadata headers = new Metadata();
            Metadata.Key<String> authKey =
                Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
            headers.put(authKey, "Bearer " + credentials);
            applier.apply(headers);
          } catch (Throwable e) {
            applier.fail(Status.UNAUTHENTICATED.withCause(e));
          }
        });
  }

  @Override
  public void thisUsesUnstableApi() {}
}
