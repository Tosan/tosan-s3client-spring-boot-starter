package com.tosan.digital.client.s3.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache5.Apache5HttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.core.interceptor.*;

import java.net.URI;

/**
 * @author saber mortazavi
 * @since 1/4/26
 */
@AutoConfiguration
@EnableConfigurationProperties(S3Properties.class)
public class S3AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(S3Client.class)
    public S3Client s3Client(
            S3Properties properties,
            @Qualifier("s3HttpClient") SdkHttpClient httpClient,
            @Qualifier("s3ObservationInterceptor") ExecutionInterceptor executionInterceptor) {
        AwsCredentialsProvider credentialsProvider =
                StringUtils.hasText(properties.getAccessKey())
                        ? StaticCredentialsProvider.create(AwsBasicCredentials
                        .create(properties.getAccessKey(), properties.getSecretKey()))
                        : DefaultCredentialsProvider.builder().build();

        boolean s3CompatibleMode = StringUtils.hasText(properties.getBaseServiceUrl());

        S3Configuration s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(s3CompatibleMode).build();

        S3ClientBuilder builder = S3Client.builder()
                .serviceConfiguration(s3Configuration)
                .credentialsProvider(credentialsProvider)
                .httpClient(httpClient)
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .addExecutionInterceptor(executionInterceptor).build());

        if (StringUtils.hasText(properties.getRegion())) {
            builder.region(Region.of(properties.getRegion()));
        }

        if (s3CompatibleMode) {
            if (!StringUtils.hasText(properties.getRegion())) {
                throw new IllegalStateException(
                        "s3 region must be set when using S3-compatible storage (SeaweedFS / MinIO ...)");
            }
            builder.endpointOverride(URI.create(properties.getBaseServiceUrl()));
        }
        return builder.build();
    }

    @Bean("s3HttpClient")
    @ConditionalOnMissingBean(SdkHttpClient.class)
    public SdkHttpClient httpClient(S3Properties properties) {
        return Apache5HttpClient.builder()
                .maxConnections(properties.getHttpClient().getMaxConnections())
                .connectionTimeout(properties.getHttpClient().getConnectionTimeout())
                .socketTimeout(properties.getHttpClient().getSocketTimeout())
                .connectionAcquisitionTimeout(properties.getHttpClient().getConnectionAcquisitionTimeout()).build();
    }

    @Bean("s3ClientLoggerUtil")
    public S3ClientLoggerUtil clientLoggerUtil() {
        return new S3ClientLoggerUtil();
    }

    @Bean("s3ObservationInterceptor")
    @ConditionalOnMissingBean(ExecutionInterceptor.class)
    public S3ObservationInterceptor observationInterceptor(
            ObservationRegistry observationRegistry,
            @Qualifier("s3ClientLoggerUtil") S3ClientLoggerUtil s3ClientLoggerUtil) {
        return new S3ObservationInterceptor(observationRegistry, s3ClientLoggerUtil);
    }
}
