package com.tosan.client.s3.starter.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class LogEntry {

    private final String invoke;
    private final String invoked;
    private final String service;
    private final Map<String, List<String>> headers;
    private final String status;
    private final Duration duration;
    private final String exception;
    private final String message;

    private LogEntry(Builder builder) {
        this.invoke = builder.invoke;
        this.invoked = builder.invoked;
        this.service = builder.service;
        this.status = builder.status;
        this.duration = builder.duration;
        this.exception = builder.exception;
        this.message = builder.message;
        this.headers = builder.headers;
    }

    public String getInvoke() {
        return invoke;
    }

    public String getInvoked() {
        return invoked;
    }

    public String getService() {
        return service;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getStatus() {
        return status;
    }

    public Duration getDuration() {
        return duration;
    }

    public String getException() {
        return exception;
    }

    public String getMessage() {
        return message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String invoke;
        private String invoked;
        private String service;
        private Map<String, List<String>> headers;
        private String status;
        private Duration duration;
        private String exception;
        private String message;

        public Builder invoke(String invoke) {
            this.invoke = invoke;
            return this;
        }

        public Builder invoked(String invoked) {
            this.invoked = invoked;
            return this;
        }

        public Builder service(String service) {
            this.service = service;
            return this;
        }

        public Builder headers(Map<String,List<String>> headers) {
            this.headers = headers;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public Builder exception(String exception) {
            this.exception = exception;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public LogEntry build() {
            return new LogEntry(this);
        }
    }
}
