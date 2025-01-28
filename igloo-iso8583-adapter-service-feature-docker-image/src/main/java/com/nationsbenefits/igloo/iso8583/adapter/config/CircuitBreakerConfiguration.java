package com.nationsbenefits.igloo.iso8583.adapter.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @author PwC
 *
 * The CircuitBreakerConfiguration class configures the Circuit Breaker and Retry mechanisms
 * for gRPC client interactions.
 * It uses the Resilience4j library to manage these configurations.
 */
@Slf4j
@Configuration
public class CircuitBreakerConfiguration {

    private final GrpcClientRetryProperties grpcClientRetryProperties;

    /**
     * Constructor for CircuitBreakerConfiguration.
     * Initializes the GrpcClientRetryProperties.
     *
     * @param grpcClientRetryProperties the properties for the gRPC client retry configuration.
     */
    public CircuitBreakerConfiguration(GrpcClientRetryProperties grpcClientRetryProperties) {
        this.grpcClientRetryProperties = grpcClientRetryProperties;
    }

    /**
     * Creates and configures a CircuitBreakerRegistry bean.
     * The Circuit Breaker configuration includes:
     * - A failure rate threshold of 50%.
     * - A wait duration in the open state of 1000 milliseconds.
     * - A sliding window size of 2.
     *
     * @return a configured CircuitBreakerRegistry instance.
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .slidingWindowSize(2)
                .build();

        return CircuitBreakerRegistry.of(config);
    }


    /**
     * Creates and configures a Retry bean.
     * The Retry configuration includes:
     * - A maximum number of retry attempts as specified in grpcClientRetryProperties.
     * - A wait duration between retry attempts as specified in grpcClientRetryProperties.
     * - Retrying on StatusRuntimeException.
     *
     * @return a configured Retry instance.
     */
    @Bean
    public Retry retryConfiguration() {
        // Create a Retry configuration
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(grpcClientRetryProperties.getMaxAttempts())
                .waitDuration(Duration.ofMillis(grpcClientRetryProperties.getWaitDuration()))
                .retryExceptions(StatusRuntimeException.class)
                .build();

        // Create a Retry instance
        return Retry.of("ISO8583AdapterRetry", retryConfig);
    }
}
