package com.nationsbenefits.igloo.authengine.config;


import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @author PwC
 *
 * Service class to manage a thread pool executor for gRPC channels.
 * This class is responsible for creating, managing, and shutting down the thread pool executor.
 */
@Slf4j
public class ChannelExecutorService {

    private final ThreadPoolExecutor executorService;

    /**
     * Constructor for ChannelExecutorService.
     * Initializes the ThreadPoolExecutor with the specified initial and maximum channel counts.
     *
     * @param initialThreadPoolCount the initial number of threads in the pool.
     * @param maxThreadPoolCount     the maximum number of threads in the pool.
     */
    public ChannelExecutorService(int initialThreadPoolCount, int maxThreadPoolCount) {
        this.executorService = new ThreadPoolExecutor(
                maxThreadPoolCount, // core pool size
                maxThreadPoolCount, // maximum pool size
                60L, TimeUnit.SECONDS, // keep-alive time for idle threads
                new LinkedBlockingQueue<>(), // work queue
                new ChannelThreadFactory(initialThreadPoolCount));  // thread factory
        // Pre-start core threads based on initialChannelCount
        this.executorService.prestartAllCoreThreads();
    }

    /**
     * Returns the ExecutorService managed by this class.
     *
     * @return the ExecutorService instance.
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }


    /**
     * Shuts down the ExecutorService.
     * This method attempts to gracefully shut down the executor service, waiting for tasks to complete.
     * If the tasks do not complete within the specified timeout, it forces a shutdown.
     */
    public void shutdown() {
        if(log.isInfoEnabled()) {
            log.info("Shutting down ChannelExecutorService...");
        }
        executorService.shutdown();
        try {
            // Wait for the executor service to terminate
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                // Force shutdown if not terminated within the timeout
                executorService.shutdownNow();
                // Wait again for termination
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("ChannelExecutorService did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // Force shutdown on interruption and re-interrupt the current thread
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
