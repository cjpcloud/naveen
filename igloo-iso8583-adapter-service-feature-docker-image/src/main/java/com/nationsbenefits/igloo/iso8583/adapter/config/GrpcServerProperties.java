package com.nationsbenefits.igloo.iso8583.adapter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * Configuration properties class for gRPC server settings.
 * This class maps properties from the application configuration file with the prefix "authengine.service".
 * It provides properties for the gRPC server host, port, and channel counts.
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "authengine.service")
public class GrpcServerProperties {

    /**
     * The port on which the gRPC server is running.
     */
    private String port;

    /**
     * The host address of the gRPC server.
     */
    private String host;

    /**
     * The initial number of threads to be created in pool.
     */
    private int initialThreadPoolCount;

    /**
     * The maximum number of threads to be created in pool.
     */
    private int maxThreadPoolCount;

    /**
     * The keepAliveTime.
     */
    private int keepAliveTime;

    /**
     * The keepAliveTimeout.
     */
    private int keepAliveTimeout;

}
