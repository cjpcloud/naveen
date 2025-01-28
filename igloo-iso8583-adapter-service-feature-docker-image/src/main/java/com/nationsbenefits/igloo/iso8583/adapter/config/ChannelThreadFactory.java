package com.nationsbenefits.igloo.iso8583.adapter.config;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @author PwC
 *
 * Custom thread factory for creating threads for gRPC channels.
 * This factory manages the creation of threads with a naming pattern and keeps track of the number of created threads.
 */
@Slf4j
public class ChannelThreadFactory implements ThreadFactory {

    private final AtomicInteger threadCount = new AtomicInteger(0);
    private final int initialChannelCount;
    private final AtomicInteger createdThreads = new AtomicInteger(0);

    /**
     * Constructor for ChannelThreadFactory.
     * Initializes the factory with the specified initial channel count.
     *
     * @param initialChannelCount the initial number of channels.
     */
    public ChannelThreadFactory(int initialChannelCount) {
        this.initialChannelCount = initialChannelCount;
    }

    /**
     * Creates a new thread to execute the given Runnable.
     * This method names the thread using a predefined naming pattern, increments the created threads count,
     * and sets an uncaught exception handler to log any exceptions thrown by the thread.
     *
     * @param r the Runnable to be executed by the new thread.
     * @return the created Thread.
     */
    @Override
    public Thread newThread(Runnable r) {
        if (createdThreads.get() < initialChannelCount) {
            createdThreads.incrementAndGet();
        }
        Thread thread = new Thread(r);
        thread.setName("ManagedChannelExecutor-Thread-" + threadCount.incrementAndGet());
        thread.setUncaughtExceptionHandler((t, e) -> {
            log.error("Thread {} threw exception: {}", t.getName(), e.getMessage(), e);
        });
        return thread;
    }
}
