package ecommerce;

import io.grpc.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;

import java.util.logging.Logger;

public class OpenTelemetryClientInterceptor implements ClientInterceptor {
    private static final Logger logger =
            Logger.getLogger(OpenTelemetryClientInterceptor.class.getName());

    // Inject context into the gRPC request metadata
    private static final TextMapSetter<Metadata> textMapSetter =
            (carrier, key, value) ->
                    carrier.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value);

    // Share context via text headers
    private final TextMapPropagator textMapPropagator =
            OpenTelemetryConfiguration.openTelemetry.getPropagators().getTextMapPropagator();

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(
                next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // Inject the request with the current context
                textMapPropagator.inject(Context.current(), headers, textMapSetter);
                // Perform the gRPC request
                super.start(responseListener, headers);
            }
        };
    }
}
