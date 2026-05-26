package com.tosan.s3client.starter.configuration;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ObservationBaseUTest {

    protected InMemorySpanExporter spanExporter;
    protected SimpleMeterRegistry meterRegistry;
    protected S3ClientObservationInterceptor interceptor;
    protected OpenTelemetrySdk openTelemetry;

    @AfterEach
    void cleanUp() {
        if (openTelemetry != null) {
            openTelemetry.close();
        }
    }
}
