package com.tosan.client.s3.starter.configuration;

import com.tosan.client.s3.starter.util.HttpStatusMessages;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.AwsResponseMetadata;
import software.amazon.awssdk.core.interceptor.*;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URI;
import java.time.Duration;
import java.util.*;


public class S3ClientObservationInterceptor implements ExecutionInterceptor {

    private static final Logger log =
            LoggerFactory.getLogger(S3ClientObservationInterceptor.class);
    private static final ExecutionAttribute<Observation> OBSERVATION_ATTRIBUTE =
            new ExecutionAttribute<>("awsObservation");
    private static final ExecutionAttribute<Long> START_TIME_ATTRIBUTE =
            new ExecutionAttribute<>("startTime");
    private static final String DEFAULT_SERVICE_NAME = "s3";
    private static final String OBSERVATION_NAME = "s3.sdk_call_service";
    private static final String UNKNOWN = "unknown";
    private static final String SERVICE_KEY = "s3.service";
    private static final String OPERATION_KEY = "s3.operation";
    private static final String BUCKET_KEY = "s3.bucket";
    private static final String REQUEST_ID_KEY = "s3.request_id";
    private static final String STATUS_CODE_KEY = "s3.status_code";
    private final ObservationRegistry observationRegistry;
    private final S3ClientLoggerUtil s3ClientLoggerUtil;

    public S3ClientObservationInterceptor(ObservationRegistry observationRegistry, S3ClientLoggerUtil s3ClientLoggerUtil) {
        this.observationRegistry = observationRegistry;
        this.s3ClientLoggerUtil = s3ClientLoggerUtil;
    }

    @Override
    public void beforeExecution(Context.BeforeExecution context, ExecutionAttributes executionAttributes) {
        String service = getOrDefault(
                getAttr(executionAttributes, SdkExecutionAttribute.SERVICE_NAME), DEFAULT_SERVICE_NAME);
        String operation = getOrDefault(
                getAttr(executionAttributes, SdkExecutionAttribute.OPERATION_NAME));
        String bucket = extractBucket(context.request());
        Observation observation = Observation.createNotStarted(OBSERVATION_NAME, observationRegistry)
                .contextualName(service + " " + operation)
                .lowCardinalityKeyValue(SERVICE_KEY, service)
                .lowCardinalityKeyValue(OPERATION_KEY, operation)
                .lowCardinalityKeyValue(BUCKET_KEY, bucket)
                .start();
        executionAttributes.putAttribute(OBSERVATION_ATTRIBUTE, observation);
        executionAttributes.putAttribute(START_TIME_ATTRIBUTE, System.nanoTime());
    }

    @Override
    public void afterExecution(Context.AfterExecution context,
                               ExecutionAttributes executionAttributes) {
        Observation observation = getAttr(executionAttributes, OBSERVATION_ATTRIBUTE);
        if (observation == null) {
            return;
        }
        if (context.response() instanceof AwsResponse response) {
            String statusCode = "OK";
            if (response.sdkHttpResponse() != null) {
                statusCode = HttpStatusMessages.getStatusMessage(response.sdkHttpResponse().statusCode());
            }
            String requestId = extractRequestId(response);
            observation
                    .lowCardinalityKeyValue(STATUS_CODE_KEY, statusCode)
                    .highCardinalityKeyValue(REQUEST_ID_KEY, getOrDefault(requestId));
        }
        observation.stop();
    }

    @Override
    public void onExecutionFailure(Context.FailedExecution context, ExecutionAttributes executionAttributes) {
        Observation observation = getAttr(executionAttributes, OBSERVATION_ATTRIBUTE);
        if (observation == null) {
            return;
        }
        observation.error(context.exception());
        String statusCode = UNKNOWN;
        String requestId = UNKNOWN;
        if (context.exception() instanceof S3Exception s3Exception) {
            statusCode = HttpStatusMessages.getStatusMessage(s3Exception.statusCode());
            requestId = s3Exception.requestId();
            observation
                    .lowCardinalityKeyValue(STATUS_CODE_KEY, statusCode)
                    .highCardinalityKeyValue(REQUEST_ID_KEY, getOrDefault(requestId));
        }
        observation.stop();
    }

    @Override
    public void beforeTransmission(Context.BeforeTransmission context, ExecutionAttributes executionAttributes) {
        SdkHttpRequest sdkHttpRequest = context.httpRequest();
        if (sdkHttpRequest != null) {
            String service = sdkHttpRequest.encodedQueryParameters().map(param ->
                    getService(sdkHttpRequest.method(), sdkHttpRequest.getUri(), param)
            ).orElseGet(() ->
                    getService(sdkHttpRequest.method(), sdkHttpRequest.getUri())
            );
            log.info(s3ClientLoggerUtil.requestLog(DEFAULT_SERVICE_NAME, service, sdkHttpRequest.headers()));
        }
    }

    @Override
    public void afterTransmission(Context.AfterTransmission context, ExecutionAttributes executionAttributes) {
        Long startTime = getAttr(executionAttributes, START_TIME_ATTRIBUTE);
        Duration duration = null;
        if (startTime != null) {
            duration = getDuration(startTime);
        }
        SdkHttpResponse sdkHttpResponse = context.httpResponse();
        if (sdkHttpResponse != null) {
            int statusCode = sdkHttpResponse.statusCode();
            String status = HttpStatusMessages.getStatusMessage(statusCode);
            String messageLog = s3ClientLoggerUtil.responseLog(DEFAULT_SERVICE_NAME, status, duration,
                    sdkHttpResponse.headers());
            if (statusCode >= 500) {
                log.error(messageLog);
            } else if (statusCode >= 400) {
                log.warn(messageLog);
            } else {
                log.info(messageLog);
            }
        }
    }

    String getService(SdkHttpMethod method, URI uri, String queryParam) {
        return String.format("%s %s?%s", method, uri, queryParam);
    }

    String getService(SdkHttpMethod method, URI uri) {
        return String.format("%s %s", method, uri);
    }


    private String extractBucket(Object request) {
        if (request == null) {
            return UNKNOWN;
        }
        if (request instanceof PutObjectRequest r) {
            return getOrDefault(r.bucket());
        }
        if (request instanceof GetObjectRequest r) {
            return getOrDefault(r.bucket());
        }
        if (request instanceof DeleteObjectRequest r) {
            return getOrDefault(r.bucket());
        }
        if (request instanceof ListObjectsV2Request r) {
            return getOrDefault(r.bucket());
        }
        return UNKNOWN;
    }

    private String getOrDefault(String value) {
        return value == null || value.isBlank() ? UNKNOWN : value;
    }

    private String getOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String extractRequestId(AwsResponse response) {
        return Optional.ofNullable(response.responseMetadata())
                .map(AwsResponseMetadata::requestId)
                .orElse(UNKNOWN);
    }

    private <T> T getAttr(ExecutionAttributes attrs, ExecutionAttribute<T> key) {
        return attrs.getAttribute(key);
    }

    private Duration getDuration(long startTime) {
        return Duration.ofNanos(Math.max(0L, (System.nanoTime() - startTime)));
    }
}
