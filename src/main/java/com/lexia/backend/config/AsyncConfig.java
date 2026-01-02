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

    /**
     * Dedicated executor for AI usage tracking.
     * Separate from main task executor to prevent resource contention
     * with other async operations.
     * Uses CallerRunsPolicy to ensure tracking is completed even under high load.
     *
     * @since Sprint 5
     */
    @Bean(name = "aiUsageExecutor")
    public Executor aiUsageExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("ai-usage-");
        // Use CallerRunsPolicy to ensure tracking completes even if queue is full
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * Dedicated executor for AI content processing (Custom Materials).
     * Uses larger queue to handle long-running AI generation tasks.
     * CallerRunsPolicy ensures processing completes even under load.
     *
     * @since Sprint 5
     */
    @Bean(name = "aiProcessingExecutor")
    public Executor aiProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("ai-processing-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * Dedicated executor for flashcard image generation.
     * Lower priority than other AI tasks, uses CallerRunsPolicy to ensure
     * completion.
     *
     * @since Sprint 6
     */
    @Bean(name = "imageGenerationExecutor")
    public Executor imageGenerationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("image-gen-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
