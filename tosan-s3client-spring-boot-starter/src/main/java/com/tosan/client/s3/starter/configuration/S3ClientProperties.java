package com.tosan.client.s3.starter.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author saber mortazavi
 * @since 1/4/26
 */

@ConfigurationProperties(prefix = "s3")
public class S3ClientProperties {

// AWS SDK always requires a region.
// For Amazon S3, this must be the real bucket region.
// For S3-compatible storage (MinIO, SeaweedFS), the region is used only for signing,
// and can be any fixed non-empty value.
    private String region;

    private String baseServiceUrl;

    private String accessKey;

    private String secretKey;

    private S3HttpClientProperties httpClient;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBaseServiceUrl() {
        return baseServiceUrl;
    }

    public void setBaseServiceUrl(String baseServiceUrl) {
        this.baseServiceUrl = baseServiceUrl;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public S3HttpClientProperties getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(S3HttpClientProperties httpClient) {
        this.httpClient = httpClient;
    }
}
