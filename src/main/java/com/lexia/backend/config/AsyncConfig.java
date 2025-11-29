package com.lexia.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration class to enable asynchronous method execution.
 * Required for @Async annotation support in AdminActivityLogService.
 *
 * @author LEXIA Team
 * @since Sprint 4
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring will use SimpleAsyncTaskExecutor by default
    // Can be customized with ThreadPoolTaskExecutor if needed
}
