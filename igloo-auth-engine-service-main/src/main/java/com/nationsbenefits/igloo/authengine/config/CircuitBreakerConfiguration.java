package com.nationsbenefits.igloo.authengine.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.grpc.StatusRuntimeException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @author PwC
 *
 * Configuration class for Circuit breaker which is used for gRPC call.
 */
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
     * Configures and provides a {@link CircuitBreakerRegistry} bean for the application context.
     * This method sets up a custom {@link CircuitBreakerConfig} for circuit breakers using Resilience4j.
     * The configuration includes:
     *     failureRateThreshold: Sets the failure rate threshold to 50%. If the failure rate
     *     exceeds this threshold, the circuit breaker transitions to the open state.
     *     waitDurationInOpenState: Sets the wait duration in the open state to 1000 milliseconds.
     *     After this duration, the circuit breaker transitions to a half-open state to test if the external service
     *     has recovered.
     *     slidingWindowSize: Sets the size of the sliding window to 2. This determines the number
     *     of calls that are used when calculating the failure rate.
     * The configured {@code CircuitBreakerRegistry} can be used throughout the application to create and manage circuit breakers
     * with the specified settings.
     *
     * @return a {@code CircuitBreakerRegistry} configured with the specified circuit breaker settings
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
        Retry retry = Retry.of("AuthEngineRetry", retryConfig);

        return retry;
    }
}
