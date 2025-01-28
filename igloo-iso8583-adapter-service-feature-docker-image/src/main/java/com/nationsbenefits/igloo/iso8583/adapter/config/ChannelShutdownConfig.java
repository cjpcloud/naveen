package com.nationsbenefits.igloo.iso8583.adapter.config;

import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @author PwC
 * The ChannelShutdownConfig class is responsible for gracefully shutting down the gRPC ManagedChannel
 * when the Spring application context is closed. It listens for the {@link ContextClosedEvent} and
 * performs the shutdown operation.
 */
@Component
@Slf4j
public class ChannelShutdownConfig implements ApplicationListener<ContextClosedEvent> {

    private final ManagedChannel managedChannel;

    /**
     * Constructs a ChannelShutdownConfig with the specified ManagedChannel.
     *
     * @param managedChannel the gRPC ManagedChannel to be shut down when the application context is closed.
     */
    @Autowired
    public ChannelShutdownConfig(ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    /**
     * Handles the {@link ContextClosedEvent} to shut down the ManagedChannel.
     * This method is called when the application context is closed.
     *
     * @param event the ContextClosedEvent indicating that the application context is being closed.
     */
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (managedChannel != null) {
            try {
                managedChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                log.info("ManagedChannel shut down successfully.");
            } catch (InterruptedException e) {
                log.error("Error shutting down ManagedChannel: ", e);
            }
        }
    }
}
