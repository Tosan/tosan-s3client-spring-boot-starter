package com.tosan.digital.client.s3.config;

import com.tosan.digital.client.s3.util.HttpStatusMessages;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.DefaultAwsResponseMetadata;
import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.SdkExecutionAttribute;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ObservationInterceptor_failureExecutionUTest extends ObservationBaseUTest {

    @BeforeEach
    void setup() {
        S3ClientLoggerUtil s3ClientLoggerUtil = new S3ClientLoggerUtil();
        spanExporter = InMemorySpanExporter.create();
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        meterRegistry = new SimpleMeterRegistry();
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                .build();
        openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
        OtelCurrentTraceContext currentTraceContext = new OtelCurrentTraceContext();
        OtelTracer tracer = new OtelTracer(
                openTelemetry.getTracer("test"),
                currentTraceContext,
                event -> {
                }
        );
        observationRegistry
                .observationConfig()
                .observationHandler(new DefaultMeterObservationHandler(meterRegistry))
                .observationHandler(new DefaultTracingObservationHandler(tracer));
        interceptor = new S3ObservationInterceptor(observationRegistry, s3ClientLoggerUtil);
    }

    @Test
    void onExecutionFailure_tracingSuccessRecorded() {
        String bucketName = "test_logs";
        String serviceName = "s3";
        String operation = "PutObject";
        String requestId = "sample_id";
        int statusCode = 400;
        PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key("test.txt").build();
        ExecutionAttributes attrs = new ExecutionAttributes();
        attrs.putAttribute(SdkExecutionAttribute.SERVICE_NAME, serviceName);
        attrs.putAttribute(SdkExecutionAttribute.OPERATION_NAME, operation);
        Context.BeforeExecution beforeContext = mock(Context.BeforeExecution.class);
        when(beforeContext.request()).thenReturn(request);
        Context.FailedExecution failedExecution = mock(Context.FailedExecution.class);
        when(failedExecution.exception()).thenReturn(NoSuchBucketException.builder()
                .statusCode(statusCode).requestId(requestId).build());
        interceptor.beforeExecution(beforeContext, attrs);
        interceptor.onExecutionFailure(failedExecution, attrs);
        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        assertThat(spans).hasSize(1);
        SpanData span = spans.getFirst();
        Assertions.assertEquals(serviceName, span.getAttributes().get(AttributeKey.stringKey("s3.service")));
        Assertions.assertEquals(operation, span.getAttributes().get(AttributeKey.stringKey("s3.operation")));
        Assertions.assertEquals(bucketName, span.getAttributes().get(AttributeKey.stringKey("s3.bucket")));
        Assertions.assertEquals(requestId, span.getAttributes().get(AttributeKey.stringKey("s3.request_id")));
        Assertions.assertEquals(HttpStatusMessages.getStatusMessage(statusCode),
                span.getAttributes().get(AttributeKey.stringKey("s3.status_code")));
    }

    @Test
    void onExecutionFailure_metricsSuccessRecorded() {
        String bucketName = "test_logs";
        String serviceName = "s3";
        String operation = "PutObject";
        int statusCode = 400;
        PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key("test.txt").build();
        ExecutionAttributes attrs = new ExecutionAttributes();
        attrs.putAttribute(SdkExecutionAttribute.SERVICE_NAME, serviceName);
        attrs.putAttribute(SdkExecutionAttribute.OPERATION_NAME, operation);
        Context.BeforeExecution beforeContext = mock(Context.BeforeExecution.class);
        when(beforeContext.request()).thenReturn(request);
        Context.FailedExecution failedExecution = mock(Context.FailedExecution.class);
        when(failedExecution.exception()).thenReturn(NoSuchBucketException.builder()
                .statusCode(statusCode).build());
        interceptor.beforeExecution(beforeContext, attrs);
        interceptor.onExecutionFailure(failedExecution, attrs);
        var timer = meterRegistry.find("s3.sdk_call_service")
                .tag("s3.bucket", bucketName)
                .tag("s3.operation", operation)
                .tag("s3.service", serviceName)
                .tag("s3.status_code", HttpStatusMessages.getStatusMessage(statusCode))
                .tag("error", "NoSuchBucketException")
                .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
    }
}
