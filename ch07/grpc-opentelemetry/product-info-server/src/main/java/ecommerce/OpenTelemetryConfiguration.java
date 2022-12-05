package ecommerce;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import java.util.concurrent.TimeUnit;

public class OpenTelemetryConfiguration {

    private static final int DEFAULT_PROMETHEUS_PORT = 19090;
    private static final String PROMETHEUS_PORT_PROPERTY = "prometheus.port";

    private static final String JAEGAR_ENDPOINT = "http://localhost:4317";

    public static OpenTelemetry initOpenTelemetry() {
        Resource resource =
                Resource.getDefault()
                        .merge(
                                Resource.create(
                                        Attributes.of(ResourceAttributes.SERVICE_NAME, "product-info-server")));

        LoggingSpanExporter loggingSpanExporter = LoggingSpanExporter.create();
        // Export traces to Jaeger over OTLP
        OtlpGrpcSpanExporter jaegarOtlpExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(JAEGAR_ENDPOINT)
                .setTimeout(30, TimeUnit.SECONDS)
                .build();
        LoggingMetricExporter metricExporter = LoggingMetricExporter.create();
        //      OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder().build();
        //      OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.builder().build();
        MetricReader prometheusReader = PrometheusHttpServer.builder().setPort(getPrometheusPort()).build();

        SdkTracerProvider sdkTracerProvider =
                SdkTracerProvider.builder()
                        .addSpanProcessor(BatchSpanProcessor.builder(loggingSpanExporter).build())
                        .addSpanProcessor(BatchSpanProcessor.builder(jaegarOtlpExporter).build())
                        .setResource(resource)
                        .build();

        SdkMeterProvider sdkMeterProvider =
                SdkMeterProvider.builder()
                        .registerMetricReader(PeriodicMetricReader.builder(metricExporter).build())
                        .registerMetricReader(prometheusReader)
                        .setResource(resource)
                        .build();

        OpenTelemetrySdk openTelemetrySdk =
                OpenTelemetrySdk.builder()
                        .setTracerProvider(sdkTracerProvider)
                        .setMeterProvider(sdkMeterProvider)
                        // install the W3C Trace Context propagator
                        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                        .buildAndRegisterGlobal();

        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    System.err.println(
                                            "*** Forcing the Trace Provider and Meter Provider to shutdown and process the remaining spans");
                                    sdkTracerProvider.shutdown();
                                    sdkMeterProvider.shutdown();
                                    System.err.println("*** TTrace Provider and Meter Provider shut down");
                                }));
        return openTelemetrySdk;
    }

    private static int getPrometheusPort() {
        return System.getProperties().containsKey(PROMETHEUS_PORT_PROPERTY) ?
                Integer.parseInt(System.getProperty(PROMETHEUS_PORT_PROPERTY)) : DEFAULT_PROMETHEUS_PORT;
    }
}
