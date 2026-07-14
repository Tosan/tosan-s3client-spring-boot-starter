package com.tosan.client.s3.starter.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;


public class S3ClientLoggerUtil {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .changeDefaultPropertyInclusion(v -> v.withValueInclusion(JsonInclude.Include.NON_NULL))
            .addModule(DurationSecondsSerializer.getModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

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
