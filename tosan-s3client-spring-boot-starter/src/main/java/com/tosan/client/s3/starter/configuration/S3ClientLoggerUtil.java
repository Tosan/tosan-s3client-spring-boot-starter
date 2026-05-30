package com.tosan.client.s3.starter.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.time.Duration;
import java.util.List;
import java.util.Map;


public class S3ClientLoggerUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(DurationSecondsSerializer.getModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public String requestLog(String invoke, String service, Map<String, List<String>> headers) {
        return toJson(LogEntry.builder().service(service).invoke(invoke).headers(headers).build());
    }

    public String responseLog(String invoked, String status, Duration duration, Map<String, List<String>> headers) {
        return toJson(LogEntry.builder().status(status).duration(duration).invoked(invoked).headers(headers)
                .build());
    }

    private String toJson(LogEntry logEntry) {
        try {
            return MAPPER.writeValueAsString(logEntry);
        } catch (Exception ex) {
            return "Error creating JSON log message: " + ex.getMessage();
        }
    }
}
