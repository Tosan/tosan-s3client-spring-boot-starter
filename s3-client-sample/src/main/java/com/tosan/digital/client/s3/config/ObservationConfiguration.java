package com.tosan.digital.client.s3.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservationConfiguration {

    @Bean
    public ObservationRegistry observationRegistryBean() {
        return ObservationRegistry.create();
    }
}
