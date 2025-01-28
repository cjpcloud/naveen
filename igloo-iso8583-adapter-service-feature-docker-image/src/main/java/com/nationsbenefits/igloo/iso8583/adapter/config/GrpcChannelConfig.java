package com.nationsbenefits.igloo.iso8583.adapter.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @author PwC
 *
 * Configuration class for setting up gRPC ManagedChannel.
 * This class is responsible for creating and configuring the ManagedChannel
 * used to communicate with the gRPC server.
 */
@Slf4j
@Configuration
public class GrpcChannelConfig {

    private final GrpcServerProperties grpcServerProperties;

    /**
     * Constructor for GrpcChannelConfig.
     * Initializes the GrpcServerProperties.
     *
     * @param grpcServerProperties the properties for the gRPC server.
     */
    public GrpcChannelConfig(GrpcServerProperties grpcServerProperties) {
        this.grpcServerProperties = grpcServerProperties;
    }

    /**
     * Bean creation method for ManagedChannel.
     * This method creates and returns a ManagedChannel for communication with the gRPC server.
     *
     * @return ManagedChannel for gRPC communication.
     */

    @Bean
    public ManagedChannel managedChannel() {
        log.info("Creating Managed Channel");
        return createManagedChannel(grpcServerProperties.getHost(), Integer.parseInt(grpcServerProperties.getPort()), grpcServerProperties.getInitialThreadPoolCount(),grpcServerProperties.getMaxThreadPoolCount());
    }

    /**
     * Creates a ManagedChannel with the specified parameters.
     * This method configures the channel with keep-alive settings and executor service.
     *
     * TODO: The below configuration to be moved to a common module so it can be accessed from all services
     *
     * @param address            the address of the gRPC server.
     * @param port               the port of the gRPC server.
     * @param initialThreadPoolCount the initial number of threads in pool.
     * @param maxThreadPoolCount     the maximum number of threads in pool.
     * @return the configured ManagedChannel.
     */
    private ManagedChannel createManagedChannel(String address, int port, int initialThreadPoolCount, int maxThreadPoolCount) {
        ChannelExecutorService channelExecutor = new ChannelExecutorService(initialThreadPoolCount, maxThreadPoolCount);
        return ManagedChannelBuilder.forAddress(address, port)
                .keepAliveTime(grpcServerProperties.getKeepAliveTime(), TimeUnit.SECONDS)// Set keep-alive time to configured value in properties
                .keepAliveTimeout(grpcServerProperties.getKeepAliveTimeout(), TimeUnit.SECONDS) // Set keep-alive timeout to configured value in properties
                .keepAliveWithoutCalls(true) // Enable keep-alive without ongoing calls
                .usePlaintext() // Use plaintext communication (no TLS)
                /**
                 * TODO: Work in Progress for TLS based communication instead of plain text.
                 * Working on acquiring the certificates that will be used for TLS based communication
                 */
                .executor(channelExecutor.getExecutorService()) // Set the executor service
                .build();
    }

}
