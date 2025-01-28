package com.nationsbenefits.igloo.authengine.config;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
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

    private final AuthEngineServerProperties authEngineServerProperties;

    /**
     * Constructor for GrpcChannelConfig.
     * Initializes the AuthEngineServerProperties.
     *
     * @param authEngineServerProperties the properties for the gRPC server.
     */
    public GrpcChannelConfig(AuthEngineServerProperties authEngineServerProperties) {
        this.authEngineServerProperties = authEngineServerProperties;
    }

    /**
     * Bean creation method for ManagedChannel.
     * This method creates and returns a ManagedChannel for communication with the Card Service gRPC server.
     *
     * @return ManagedChannel for gRPC communication.
     */
    @Bean
    public ManagedChannel cardServiceChannel() {
        if(log.isInfoEnabled()) {
            log.info("Creating Card Service Managed Channel..");
        }
        return createManagedChannel(authEngineServerProperties.getHost().getCard(), authEngineServerProperties.getPort().getCard(), authEngineServerProperties.getInitialThreadPoolCount(),authEngineServerProperties.getMaxThreadPoolCount());
    }


    /**
     * Bean creation method for ManagedChannel.
     * This method creates and returns a ManagedChannel for communication with the Transaction Fraud Analyzer Service gRPC server.
     *
     * @return ManagedChannel for gRPC communication.
     */
    @Bean
    public ManagedChannel txnFraudAnalyzerServiceChannel() {
        if(log.isInfoEnabled()) {
            log.info("Creating Txn Fraud Analyzer Service Managed Channel..");
        }
        return createManagedChannel(authEngineServerProperties.getHost().getTxnFraud(), authEngineServerProperties.getPort().getTxnFraud(), authEngineServerProperties.getInitialThreadPoolCount(),authEngineServerProperties.getMaxThreadPoolCount());
    }

    /**
     * Bean creation method for ManagedChannel.
     * This method creates and returns a ManagedChannel for communication with the Member Service gRPC server.
     *
     * @return ManagedChannel for gRPC communication.
     */
    @Bean
    public ManagedChannel memberServiceChannel() {
        if(log.isInfoEnabled()) {
            log.info("Creating Member Service Managed Channel..");
        }
        return createManagedChannel(authEngineServerProperties.getHost().getMember(), authEngineServerProperties.getPort().getMember(), authEngineServerProperties.getInitialThreadPoolCount(),authEngineServerProperties.getMaxThreadPoolCount());
    }


    /**
     * Bean creation method for ManagedChannel.
     * This method creates and returns a ManagedChannel for communication with the Member Account Service gRPC server.
     *
     * @return ManagedChannel for gRPC communication.
     */
    @Bean
    public ManagedChannel memberAccountServiceChannel() {
        if(log.isInfoEnabled()) {
            log.info("Creating Member Account Service Managed Channel..");
        }
        return createManagedChannel(authEngineServerProperties.getHost().getMemberAccount(), authEngineServerProperties.getPort().getMemberAccount(), authEngineServerProperties.getInitialThreadPoolCount(),authEngineServerProperties.getMaxThreadPoolCount());
    }


    /**
     * Bean creation method for ManagedChannel.
     * This method creates and returns a ManagedChannel for communication with the BAS gRPC server.
     *
     * @return ManagedChannel for gRPC communication.
     */
    @Bean
    public ManagedChannel basServiceChannel() {
        if(log.isInfoEnabled()) {
            log.info("Creating BAS Service Managed Channel..");
        }
        return createManagedChannel(authEngineServerProperties.getHost().getBAS(), authEngineServerProperties.getPort().getBAS(), authEngineServerProperties.getInitialThreadPoolCount(),authEngineServerProperties.getMaxThreadPoolCount());
    }


    /**
     * Bean creation method for ManagedChannel.
     * This method creates and returns a ManagedChannel for communication with the Ledger Service gRPC server.
     *
     * @return ManagedChannel for gRPC communication.
     */
    @Bean
    public ManagedChannel ledgerServiceChannel() {
        if(log.isInfoEnabled()) {
            log.info("Creating Ledger Service Managed Channel..");
        }
        return createManagedChannel(authEngineServerProperties.getHost().getLedger(), authEngineServerProperties.getPort().getLedger(), authEngineServerProperties.getInitialThreadPoolCount(),authEngineServerProperties.getMaxThreadPoolCount());
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
                .keepAliveTime(authEngineServerProperties.getKeepAliveTime(), TimeUnit.SECONDS)// Set keep-alive time to configured value in properties
                .keepAliveTimeout(authEngineServerProperties.getKeepAliveTimeout(), TimeUnit.SECONDS) // Set keep-alive timeout to configured value in properties
                .keepAliveWithoutCalls(true) // Enable keep-alive without ongoing calls
                .disableServiceConfigLookUp()
                /**
                 * TODO: Work in Progress for TLS based communication instead of plain text.
                 * Working on acquiring the certificates that will be used for TLS based communication
                 */
                .usePlaintext() // Use plaintext communication (no TLS)
                .executor(channelExecutor.getExecutorService()) // Set the executor service
                .build();
    }

}
