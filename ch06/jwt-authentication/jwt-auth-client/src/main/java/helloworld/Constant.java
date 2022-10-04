package helloworld;

import io.grpc.Context;
import io.grpc.Metadata;

public final class Constant {
  static final String JWT_SIGNING_KEY = "L8hHXsaQOUjk5rg7XPGv4eL36anlCrkMz8CJ0i/8E/0=";
  static final String BEARER_TYPE = "Bearer";

  static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY =
      Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
  static final Context.Key<String> CLIENT_ID_CONTEXT_KEY = Context.key("clientId");
}
