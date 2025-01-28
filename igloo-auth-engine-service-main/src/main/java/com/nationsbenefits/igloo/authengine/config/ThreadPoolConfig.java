package com.nationsbenefits.igloo.authengine.config;

import com.nationsbenefits.igloo.authengine.constant.AuthEngineConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration class for setting up the gRPC related thread pool.
 * This configuration is used to define the Executor service that will handle
 * asynchronous gRPC calls within the application.
 *
 * <p>The class is annotated with {@link Configuration} to indicate that it is a
 * Spring configuration class. It contains a bean definition for the gRPC thread pool
 * executor which is used to manage and execute gRPC service calls asynchronously.</p>
 *
 * <p>The thread pool executor is configured with a core pool size, maximum pool size,
 * queue capacity, and thread name prefix to ensure efficient handling of concurrent tasks.</p>
 *
 * Copyright Â© 2024, NationsBenefits. All Rights reserved
 * @author PwC
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * Defines the gRPC thread pool executor bean.
     *
     * <p>This method configures and returns a {@link ThreadPoolTaskExecutor} which is
     * used to handle gRPC related asynchronous calls. The executor is configured with
     * the following parameters:
     * <ul>
     *     <li>Core Pool Size: 10</li>
     *     <li>Max Pool Size: 20</li>
     *     <li>Queue Capacity: 50</li>
     *     <li>Thread Name Prefix: {@link AuthEngineConstants#GRPC_THREAD_NAME}</li>
     * </ul>
     * </p>
     *
     * <p>The core pool size defines the minimum number of threads that are always kept alive,
     * even if they are idle. The maximum pool size defines the maximum number of threads that
     * can be created. The queue capacity defines the number of tasks that can be queued for
     * execution. The thread name prefix is used to set the name of each thread in the pool,
     * which helps in identifying and debugging thread-related issues.</p>
     *
     * @return the configured {@link Executor} for gRPC related asynchronous calls
     */
    @Bean(name = "grpcThreadPool")
    public Executor grpcThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix(AuthEngineConstants.GRPC_THREAD_NAME);
        executor.initialize();
        return executor;
    }
}
