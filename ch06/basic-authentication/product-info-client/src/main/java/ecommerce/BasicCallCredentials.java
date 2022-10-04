package ecommerce;

import java.util.Base64;
import java.util.concurrent.Executor;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;

public class BasicCallCredentials extends CallCredentials {
  private final String credentials;

  public BasicCallCredentials(String username, String password) {
    this.credentials = username + ":" + password;
  }

  @Override
  public void applyRequestMetadata(
      RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
    appExecutor.execute(
        () -> {
          try {
            Metadata headers = new Metadata();
            Metadata.Key<String> authKey =
                Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
            headers.put(
                authKey, "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes()));
            applier.apply(headers);
          } catch (Throwable e) {
            applier.fail(Status.UNAUTHENTICATED.withCause(e));
          }
        });
  }

  @Override
  public void thisUsesUnstableApi() {}
}
