package echo;

import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

public class MultiAddressNameResolverProvider extends NameResolverProvider {
  private final List<EquivalentAddressGroup> addressGroups;

  public MultiAddressNameResolverProvider(SocketAddress... socketAddresses) {
    this.addressGroups =
        Arrays.stream(socketAddresses)
            .map(EquivalentAddressGroup::new)
            .collect(Collectors.toList());
  }

  @Override
  protected boolean isAvailable() {
    return true;
  }

  @Override
  protected int priority() {
    return 0;
  }

  @Override
  public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
    return new NameResolver() {
      @Override
      public String getServiceAuthority() {
        return "fakeAuthority";
      }

      @Override
      public void shutdown() {}

      @Override
      public void start(Listener2 listener) {
        listener.onResult(
            ResolutionResult.newBuilder()
                .setAddresses(addressGroups)
                .setAttributes(Attributes.EMPTY)
                .build());
      }
    };
  }

  @Override
  public String getDefaultScheme() {
    return "multiaddress";
  }
}
