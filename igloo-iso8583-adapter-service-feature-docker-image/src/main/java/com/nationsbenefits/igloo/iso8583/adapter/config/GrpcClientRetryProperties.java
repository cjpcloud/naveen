package com.nationsbenefits.igloo.iso8583.adapter.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @author PwC
 *
 * Configuration properties class for gRPC server retry settings.
 * This class maps properties from the application configuration file with the prefix "retry.config".
 * It provides properties for the gRPC server retry configuration
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "retry.config")
public class GrpcClientRetryProperties {

    /**
     * The maximum number of attemots to be taken for retry
     */
    private int maxAttempts;

    /**
     * The wait duration before retry
     */
    private int waitDuration;
}
