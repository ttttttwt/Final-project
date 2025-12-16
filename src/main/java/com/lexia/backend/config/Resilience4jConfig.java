package com.lexia.backend.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration class for Resilience4j event logging.
 * 
 * <p>Registers event consumers for:</p>
 * <ul>
 *   <li>Retry events - logs each retry attempt with details</li>
 *   <li>Circuit breaker events - logs state transitions</li>
 *   <li>Rate limiter events - logs rate limit events</li>
 * </ul>
 * 
 * @see io.github.resilience4j.retry.RetryRegistry
 * @see io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
 */
@Configuration
public class Resilience4jConfig {

    private static final Logger log = LoggerFactory.getLogger(Resilience4jConfig.class);

    /** Gemini API instance name (matches application.properties) */
    private static final String GEMINI_API_INSTANCE = "geminiApi";

    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;

    public Resilience4jConfig(
            RetryRegistry retryRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RateLimiterRegistry rateLimiterRegistry) {
        this.retryRegistry = retryRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    /**
     * Registers event consumers for all Resilience4j components after bean initialization.
     */
    @PostConstruct
    public void registerEventConsumers() {
        registerRetryEventConsumers();
        registerCircuitBreakerEventConsumers();
        registerRateLimiterEventConsumers();
        log.info("Resilience4j event consumers registered successfully");
    }

    /**
     * Registers retry event consumers for detailed retry logging.
     * Logs each retry attempt including attempt number, wait interval, and exception details.
     */
    private void registerRetryEventConsumers() {
        retryRegistry.retry(GEMINI_API_INSTANCE).getEventPublisher()
                .onRetry(event -> log.warn(
                        "[RETRY] Gemini API - Attempt #{} of {}. " +
                        "Waiting {}ms before next attempt. " +
                        "Exception: {} - {}",
                        event.getNumberOfRetryAttempts(),
                        retryRegistry.retry(GEMINI_API_INSTANCE).getRetryConfig().getMaxAttempts(),
                        event.getWaitInterval().toMillis(),
                        event.getLastThrowable().getClass().getSimpleName(),
                        event.getLastThrowable().getMessage()
                ))
                .onSuccess(event -> log.info(
                        "[RETRY] Gemini API - Success after {} attempt(s). Total duration: {}ms",
                        event.getNumberOfRetryAttempts(),
                        event.getNumberOfRetryAttempts() > 0 ? "with retries" : "first attempt"
                ))
                .onError(event -> log.error(
                        "[RETRY] Gemini API - All {} retry attempts exhausted. " +
                        "Final exception: {} - {}",
                        event.getNumberOfRetryAttempts(),
                        event.getLastThrowable().getClass().getSimpleName(),
                        event.getLastThrowable().getMessage()
                ))
                .onIgnoredError(event -> log.debug(
                        "[RETRY] Gemini API - Error ignored (not retryable): {} - {}",
                        event.getLastThrowable().getClass().getSimpleName(),
                        event.getLastThrowable().getMessage()
                ));
    }

    /**
     * Registers circuit breaker event consumers for state transition logging.
     */
    private void registerCircuitBreakerEventConsumers() {
        circuitBreakerRegistry.circuitBreaker(GEMINI_API_INSTANCE).getEventPublisher()
                .onStateTransition(event -> log.warn(
                        "[CIRCUIT-BREAKER] Gemini API - State changed from {} to {}",
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()
                ))
                .onFailureRateExceeded(event -> log.error(
                        "[CIRCUIT-BREAKER] Gemini API - Failure rate exceeded: {}%",
                        event.getFailureRate()
                ))
                .onSlowCallRateExceeded(event -> log.warn(
                        "[CIRCUIT-BREAKER] Gemini API - Slow call rate exceeded: {}%",
                        event.getSlowCallRate()
                ))
                .onCallNotPermitted(event -> log.warn(
                        "[CIRCUIT-BREAKER] Gemini API - Call not permitted, circuit is OPEN"
                ));
    }

    /**
     * Registers rate limiter event consumers for rate limiting events.
     */
    private void registerRateLimiterEventConsumers() {
        rateLimiterRegistry.rateLimiter(GEMINI_API_INSTANCE).getEventPublisher()
                .onSuccess(event -> log.debug(
                        "[RATE-LIMITER] Gemini API - Request permitted"
                ))
                .onFailure(event -> log.warn(
                        "[RATE-LIMITER] Gemini API - Request rejected due to rate limit"
                ));
    }
}
