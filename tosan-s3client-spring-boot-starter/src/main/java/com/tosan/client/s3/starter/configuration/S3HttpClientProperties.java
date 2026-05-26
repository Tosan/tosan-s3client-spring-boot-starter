package com.tosan.client.s3.starter.configuration;

import java.time.Duration;

/**
 * @author saber mortazavi
 * @since 1/4/26
 */

public class S3HttpClientProperties {

    private int maxConnections = 100;

    private Duration connectionTimeout = Duration.ofSeconds(10);

    private Duration socketTimeout = Duration.ofSeconds(20);

    private Duration connectionAcquisitionTimeout = Duration.ofSeconds(10);

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Duration getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(Duration socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public Duration getConnectionAcquisitionTimeout() {
        return connectionAcquisitionTimeout;
    }

    public void setConnectionAcquisitionTimeout(Duration connectionAcquisitionTimeout) {
        this.connectionAcquisitionTimeout = connectionAcquisitionTimeout;
    }
}
