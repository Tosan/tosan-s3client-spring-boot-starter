package com.tosan.client.s3.starter.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.time.Duration;


public class S3ClientLoggerUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(DurationSecondsSerializer.getModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public String beforeExecution(String invoke, String service, String bucket) {
        return toJson(base(service, bucket)
                .invoke(invoke)
                .duration(Duration.ofSeconds(0))
                .build());
    }

    public String afterExecution(String invoked,
                                 String service,
                                 String bucket,
                                 String status,
                                 Duration duration,
                                 String requestId) {
        return toJson(base(service, bucket)
                .invoked(invoked)
                .status(status)
                .duration(duration)
                .requestId(requestId)
                .build());
    }

    public String onExecutionFailure(String invoked,
                                     String service,
                                     String bucket,
                                     String statusCode,
                                     Duration duration,
                                     Throwable throwable,
                                     String requestId) {
        return toJson(base(service, bucket)
                .invoked(invoked)
                .status(statusCode)
                .duration(duration)
                .requestId(requestId)
                .exception(throwable != null ? throwable.getClass().getSimpleName() : null)
                .message(throwable != null ? throwable.getMessage() : null)
                .build());
    }

    private String toJson(LogEntry logEntry) {
        try {
            return MAPPER.writeValueAsString(logEntry);
        } catch (Exception ex) {
            return "Error creating JSON log message: " + ex.getMessage();
        }
    }

    private LogEntry.Builder base(String service, String bucket) {
        return LogEntry.builder()
                .service(service)
                .bucket(bucket);
    }
}
