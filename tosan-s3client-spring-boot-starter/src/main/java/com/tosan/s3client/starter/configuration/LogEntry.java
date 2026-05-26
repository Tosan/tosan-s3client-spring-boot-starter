package com.tosan.s3client.starter.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Duration;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class LogEntry {

    private final String invoke;
    private final String invoked;
    private final String service;
    private final String bucket;
    private final String status;
    private final Duration duration;
    private final String requestId;
    private final String exception;
    private final String message;

    private LogEntry(Builder builder) {
        this.invoke = builder.invoke;
        this.invoked = builder.invoked;
        this.service = builder.service;
        this.bucket = builder.bucket;
        this.status = builder.status;
        this.duration = builder.duration;
        this.requestId = builder.requestId;
        this.exception = builder.exception;
        this.message = builder.message;
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

    public String getBucket() {
        return bucket;
    }

    public String getStatus() {
        return status;
    }

    public Duration getDuration() {
        return duration;
    }

    public String getRequestId() {
        return requestId;
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
        private String bucket;
        private String status;
        private Duration duration;
        private String requestId;
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

        public Builder bucket(String bucket) {
            this.bucket = bucket;
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

        public Builder requestId(String requestId) {
            this.requestId = requestId;
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
