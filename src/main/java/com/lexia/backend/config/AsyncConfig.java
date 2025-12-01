package com.lexia.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration class to enable asynchronous method execution.
 * Required for @Async annotation support in services.
 *
 * @author LEXIA Team
 * @since Sprint 4
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Default async executor for general async operations.
     * Used by AdminActivityLogService and other services.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }

    /**
     * Dedicated executor for file access recording.
     * Low priority, non-critical operations.
     * Uses smaller pool to avoid resource contention.
     */
    @Bean(name = "fileAccessExecutor")
    public Executor fileAccessExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("file-access-");
        // Allow tasks to be discarded if queue is full (access recording is
        // non-critical)
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }
}
