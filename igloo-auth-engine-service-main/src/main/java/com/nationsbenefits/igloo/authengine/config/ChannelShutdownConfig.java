package com.nationsbenefits.igloo.authengine.config;

import io.grpc.ManagedChannel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @author PwC
 *
 * The ChannelShutdownConfig class is responsible for gracefully shutting down the gRPC ManagedChannel
 * when the Spring application context is closed. It listens for the {@link ContextClosedEvent} and
 * performs the shutdown operation.
 */
@Component
@RequiredArgsConstructor
public class ChannelShutdownConfig implements ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ChannelShutdownConfig.class);

    private final ManagedChannel cardServiceChannel;
    private final ManagedChannel txnFraudAnalyzerServiceChannel;

    private final ManagedChannel memberServiceChannel;

    private final ManagedChannel memberAccountServiceChannel;

    private final ManagedChannel basServiceChannel;

    private final ManagedChannel ledgerServiceChannel;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (cardServiceChannel != null) {
            try {
                cardServiceChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                logger.info("ManagedChannel shut down successfully.");
            } catch (InterruptedException e) {
                logger.error("Error shutting down ManagedChannel: ", e);
            }
        }if (txnFraudAnalyzerServiceChannel != null) {
            try {
                txnFraudAnalyzerServiceChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                logger.info("ManagedChannel shut down successfully.");
            } catch (InterruptedException e) {
                logger.error("Error shutting down ManagedChannel: ", e);
            }
        }if (memberServiceChannel != null) {
            try {
                memberServiceChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                logger.info("ManagedChannel shut down successfully.");
            } catch (InterruptedException e) {
                logger.error("Error shutting down ManagedChannel: ", e);
            }
        }if (memberAccountServiceChannel != null) {
            try {
                memberAccountServiceChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                logger.info("ManagedChannel shut down successfully.");
            } catch (InterruptedException e) {
                logger.error("Error shutting down ManagedChannel: ", e);
            }
        }if (basServiceChannel != null) {
            try {
                basServiceChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                logger.info("ManagedChannel shut down successfully.");
            } catch (InterruptedException e) {
                logger.error("Error shutting down ManagedChannel: ", e);
            }
        }if (ledgerServiceChannel != null) {
            try {
                ledgerServiceChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                if(logger.isInfoEnabled()) {
                    logger.info("ManagedChannel shut down successfully.");
                }
            } catch (InterruptedException e) {
                logger.error("Error shutting down ManagedChannel: ", e);
            }
        }
    }
}
