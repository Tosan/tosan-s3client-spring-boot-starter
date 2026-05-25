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
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.awscore.DefaultAwsResponseMetadata;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;

import org.junit.jupiter.api.BeforeEach;
import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.SdkExecutionAttribute;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3ResponseMetadata;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;

class S3ObservationInterceptor_afterExecutionUTest extends ObservationBaseUTest {

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
    void afterExecution_tracingSuccessRecorded() {
        String bucketName = "test_logs";
        String serviceName = "s3";
        String operation = "PutObject";
        String requestId = "sample_id";
        int statusCode = 400;
        PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key("test.txt").build();
        PutObjectResponse response = mock(PutObjectResponse.class);
        SdkHttpResponse httpResponse = mock(SdkHttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(statusCode);
        when(response.sdkHttpResponse()).thenReturn(httpResponse);
        Map<String, String> map = Map.of("x-amz-request-id", requestId);
        when(response.responseMetadata()).thenReturn(
                S3ResponseMetadata.create(DefaultAwsResponseMetadata.create(
                        map
                )));
        ExecutionAttributes attrs = new ExecutionAttributes();
        attrs.putAttribute(SdkExecutionAttribute.SERVICE_NAME, serviceName);
        attrs.putAttribute(SdkExecutionAttribute.OPERATION_NAME, operation);
        Context.BeforeExecution beforeContext = mock(Context.BeforeExecution.class);
        when(beforeContext.request()).thenReturn(request);
        Context.AfterExecution afterContext = mock(Context.AfterExecution.class);
        when(afterContext.response()).thenReturn(response);
        interceptor.beforeExecution(beforeContext, attrs);
        interceptor.afterExecution(afterContext, attrs);
        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        assertThat(spans).hasSize(1);
        SpanData span = spans.get(0);
        Assertions.assertEquals(serviceName, span.getAttributes().get(AttributeKey.stringKey("s3.service")));
        Assertions.assertEquals(operation, span.getAttributes().get(AttributeKey.stringKey("s3.operation")));
        Assertions.assertEquals(bucketName, span.getAttributes().get(AttributeKey.stringKey("s3.bucket")));
        Assertions.assertEquals(requestId, span.getAttributes().get(AttributeKey.stringKey("s3.request_id")));
        Assertions.assertEquals(HttpStatusMessages.getStatusMessage(statusCode),
                span.getAttributes().get(AttributeKey.stringKey("s3.status_code")));

    }

    @Test
    void afterExecution_metricsSuccessRecorded() {
        String bucketName = "test_logs";
        String serviceName = "s3";
        String operation = "PutObject";
        int statusCode = 200;
        PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key("test.txt").build();
        PutObjectResponse response = mock(PutObjectResponse.class);
        S3ResponseMetadata s3ResponseMetadata = S3ResponseMetadata.create(
                DefaultAwsResponseMetadata.create(
                        Map.of("x-amz-request-id", "sample_id")
                ));
        when(response.responseMetadata()).thenReturn(s3ResponseMetadata);
        SdkHttpResponse httpResponse = mock(SdkHttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(statusCode);
        when(response.sdkHttpResponse()).thenReturn(httpResponse);
        ExecutionAttributes attrs = new ExecutionAttributes();
        attrs.putAttribute(SdkExecutionAttribute.SERVICE_NAME, serviceName);
        attrs.putAttribute(SdkExecutionAttribute.OPERATION_NAME, operation);
        Context.BeforeExecution beforeContext = mock(Context.BeforeExecution.class);
        when(beforeContext.request()).thenReturn(request);
        Context.AfterExecution afterContext = mock(Context.AfterExecution.class);
        when(afterContext.response()).thenReturn(response);
        interceptor.beforeExecution(beforeContext, attrs);
        interceptor.afterExecution(afterContext, attrs);
        var timer = meterRegistry.find("s3.sdk_call_service")
                .tag("s3.bucket", bucketName)
                .tag("s3.operation", operation)
                .tag("s3.service", serviceName)
                .tag("s3.status_code", HttpStatusMessages.getStatusMessage(statusCode))
                .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
    }
}
