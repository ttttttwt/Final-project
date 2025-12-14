package com.lexia.backend.service.ai;

import com.google.genai.Client;
import com.google.genai.types.Candidate;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentResponseUsageMetadata;
import com.google.genai.types.Part;
import com.lexia.backend.config.GeminiConfig;
import com.lexia.backend.dto.ai.GeminiResponseDTO;
import com.lexia.backend.dto.ai.SseEventDTO;
import com.lexia.backend.dto.ai.TokenUsageDTO;
import com.lexia.backend.exception.ai.AiConfigurationException;
import com.lexia.backend.exception.ai.AiRateLimitException;
import com.lexia.backend.exception.ai.AiServiceException;
import com.lexia.backend.exception.ai.AiTimeoutException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of GeminiClientService with resilience patterns.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Retry with exponential backoff (3 attempts)</li>
 *   <li>Circuit breaker (opens at 50% failure rate)</li>
 *   <li>Rate limiting (100 requests/minute)</li>
 *   <li>SSE streaming for real-time responses</li>
 *   <li>Fallback content on failures</li>
 * </ul>
 * 
 * @see GeminiClientService
 * @see GeminiConfig
 */
@Service
public class GeminiClientServiceImpl implements GeminiClientService {

    private static final Logger log = LoggerFactory.getLogger(GeminiClientServiceImpl.class);

    /** Gemini API circuit breaker name (matches application.properties) */
    private static final String CIRCUIT_BREAKER_NAME = "geminiApi";

    /** SSE emitter timeout (30 seconds for streaming) */
    private static final long SSE_TIMEOUT_MS = 30_000L;

    /** Token estimation: average characters per token for English */
    private static final double CHARS_PER_TOKEN = 4.0;

    /** Gemini pricing: input tokens per million (gemini-2.0-flash-exp) */
    private static final double INPUT_COST_PER_MILLION = 0.075;

    /** Gemini pricing: output tokens per million (gemini-2.0-flash-exp) */
    private static final double OUTPUT_COST_PER_MILLION = 0.30;

    private final Client geminiClient;
    private final GeminiConfig geminiConfig;
    private final GenerateContentConfig defaultContentConfig;
    private final GenerateContentConfig structuredContentConfig;
    private final GenerateContentConfig creativeContentConfig;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final ExecutorService streamExecutor;

    public GeminiClientServiceImpl(
            @Nullable Client geminiClient,
            GeminiConfig geminiConfig,
            GenerateContentConfig defaultContentConfig,
            GenerateContentConfig structuredContentConfig,
            GenerateContentConfig creativeContentConfig,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.geminiClient = geminiClient;
        this.geminiConfig = geminiConfig;
        this.defaultContentConfig = defaultContentConfig;
        this.structuredContentConfig = structuredContentConfig;
        this.creativeContentConfig = creativeContentConfig;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.streamExecutor = Executors.newCachedThreadPool();

        log.info("GeminiClientService initialized. API configured: {}", isConfigured());
    }

    /**
     * Gracefully shuts down the executor service when the application stops.
     * Ensures all streaming threads are properly terminated.
     */
    @PreDestroy
    public void shutdown() {
        if (streamExecutor != null && !streamExecutor.isShutdown()) {
            log.info("Shutting down SSE stream executor...");
            streamExecutor.shutdown();
            try {
                if (!streamExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("SSE executor did not terminate gracefully, forcing shutdown");
                    streamExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.warn("SSE executor shutdown interrupted, forcing shutdown");
                streamExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("SSE stream executor shutdown complete");
        }
    }

    @Override
    @Retry(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "generateContentFallback")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "generateContentFallback")
    @RateLimiter(name = CIRCUIT_BREAKER_NAME)
    public GeminiResponseDTO generateContent(String prompt) {
        return generateContent(prompt, geminiConfig.getDefaultModel());
    }

    @Override
    @Retry(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "generateContentFallback")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "generateContentFallback")
    @RateLimiter(name = CIRCUIT_BREAKER_NAME)
    public GeminiResponseDTO generateContent(String prompt, String model) {
        return generateContent(prompt, model, geminiConfig.getTemperature(), geminiConfig.getMaxOutputTokens());
    }

    @Override
    @Retry(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "generateContentFallback")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "generateContentFallback")
    @RateLimiter(name = CIRCUIT_BREAKER_NAME)
    public GeminiResponseDTO generateContent(String prompt, String model, float temperature, int maxTokens) {
        validateConfiguration();
        validatePrompt(prompt);

        log.debug("Generating content with model: {}, temperature: {}, maxTokens: {}", 
                  model, temperature, maxTokens);

        long startTime = System.currentTimeMillis();

        try {
            // Build custom config if needed
            GenerateContentConfig config = buildConfig(temperature, maxTokens);

            // Create content request
            Content content = Content.builder()
                    .role("user")
                    .parts(Part.fromText(prompt))
                    .build();

            // Generate response
            GenerateContentResponse response = geminiClient.models.generateContent(model, content, config);

            long responseTimeMs = System.currentTimeMillis() - startTime;

            // Extract response data
            String generatedText = extractText(response);
            TokenUsageDTO tokenUsage = extractTokenUsage(response, prompt, generatedText);
            String finishReason = extractFinishReason(response);

            log.info("Content generated successfully. Model: {}, Tokens: {}, Time: {}ms",
                     model, tokenUsage.totalTokens(), responseTimeMs);

            return GeminiResponseDTO.success(generatedText, model, tokenUsage, responseTimeMs, finishReason);

        } catch (Exception e) {
            log.error("Error generating content: {}", e.getMessage(), e);
            throw mapException(e);
        }
    }

    @Override
    @Async
    public CompletableFuture<GeminiResponseDTO> generateContentAsync(String prompt) {
        return generateContentAsync(prompt, geminiConfig.getDefaultModel());
    }

    @Override
    @Async
    public CompletableFuture<GeminiResponseDTO> generateContentAsync(String prompt, String model) {
        return CompletableFuture.supplyAsync(() -> generateContent(prompt, model));
    }

    @Override
    public SseEmitter streamContent(String prompt) {
        return streamContent(prompt, geminiConfig.getDefaultModel());
    }

    @Override
    public SseEmitter streamContent(String prompt, String model) {
        validateConfiguration();
        validatePrompt(prompt);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        String messageId = UUID.randomUUID().toString();

        // Handle lifecycle events
        emitter.onCompletion(() -> log.debug("SSE stream completed for message: {}", messageId));
        emitter.onTimeout(() -> {
            log.warn("SSE stream timed out for message: {}", messageId);
            sendError(emitter, "Stream timed out");
        });
        emitter.onError(e -> log.error("SSE stream error for message {}: {}", messageId, e.getMessage()));

        // Execute streaming in background
        streamExecutor.submit(() -> executeStreaming(emitter, prompt, model, messageId));

        return emitter;
    }

    /**
     * Executes the streaming operation in a background thread.
     */
    private void executeStreaming(SseEmitter emitter, String prompt, String model, String messageId) {
        int tokenIndex = 0;
        StringBuilder fullResponse = new StringBuilder();

        try {
            GenerateContentConfig config = creativeContentConfig;

            Content content = Content.builder()
                    .role("user")
                    .parts(Part.fromText(prompt))
                    .build();

            // Use streaming API
            Iterable<GenerateContentResponse> stream = geminiClient.models.generateContentStream(model, content, config);

            for (GenerateContentResponse chunk : stream) {
                String text = extractText(chunk);
                if (text != null && !text.isEmpty()) {
                    fullResponse.append(text);
                    sendToken(emitter, text, tokenIndex++);
                }
            }

            // Send completion event
            int totalTokens = estimateTokens(prompt) + estimateTokens(fullResponse.toString());
            sendComplete(emitter, messageId, totalTokens);

            log.info("Streaming completed for message: {}. Total tokens: {}", messageId, totalTokens);

        } catch (Exception e) {
            log.error("Streaming error for message {}: {}", messageId, e.getMessage(), e);
            sendError(emitter, e.getMessage());
        }
    }

    /**
     * Sends a token event to the SSE emitter.
     */
    private void sendToken(SseEmitter emitter, String content, int index) {
        try {
            SseEventDTO event = SseEventDTO.token(content, index);
            emitter.send(SseEmitter.event()
                    .name("token")
                    .data(event));
        } catch (IOException e) {
            log.warn("Failed to send token event: {}", e.getMessage());
        }
    }

    /**
     * Sends a completion event to the SSE emitter.
     */
    private void sendComplete(SseEmitter emitter, String messageId, int totalTokens) {
        try {
            SseEventDTO event = SseEventDTO.complete(messageId, totalTokens);
            emitter.send(SseEmitter.event()
                    .name("complete")
                    .data(event));
            emitter.complete();
        } catch (IOException e) {
            log.warn("Failed to send complete event: {}", e.getMessage());
        }
    }

    /**
     * Sends an error event to the SSE emitter.
     */
    private void sendError(SseEmitter emitter, String errorMessage) {
        try {
            SseEventDTO event = SseEventDTO.error(errorMessage);
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(event));
            emitter.completeWithError(new AiServiceException(errorMessage));
        } catch (IOException e) {
            log.warn("Failed to send error event: {}", e.getMessage());
        }
    }

    @Override
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        // Heuristic: ~4 characters per token for English
        return (int) Math.ceil(text.length() / CHARS_PER_TOKEN);
    }

    @Override
    public boolean isConfigured() {
        return geminiConfig.isConfigured() && geminiClient != null;
    }

    @Override
    public boolean isHealthy() {
        if (!isConfigured()) {
            return false;
        }
        io.github.resilience4j.circuitbreaker.CircuitBreaker cb = 
                circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);
        return cb.getState() != io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN;
    }

    @Override
    public String getCircuitBreakerState() {
        io.github.resilience4j.circuitbreaker.CircuitBreaker cb = 
                circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);
        return cb.getState().name();
    }

    // ==================== Fallback Methods ====================

    /**
     * Fallback method when content generation fails.
     * Returns predefined fallback content.
     */
    @SuppressWarnings("unused")
    private GeminiResponseDTO generateContentFallback(String prompt, Exception e) {
        log.warn("Fallback triggered for default model. Error type: {}", e.getClass().getSimpleName());
        return GeminiResponseDTO.fallback(
                "I apologize, but I'm temporarily unable to generate a response. Please try again later.",
                geminiConfig.getDefaultModel()
        );
    }

    /**
     * Fallback method for model-specific generation.
     */
    @SuppressWarnings("unused")
    private GeminiResponseDTO generateContentFallback(String prompt, String model, Exception e) {
        log.warn("Fallback triggered for model {}. Error type: {}", model, e.getClass().getSimpleName());
        return GeminiResponseDTO.fallback(
                "I apologize, but I'm temporarily unable to generate a response. Please try again later.",
                model
        );
    }

    /**
     * Fallback method for custom config generation.
     */
    @SuppressWarnings("unused")
    private GeminiResponseDTO generateContentFallback(String prompt, String model, 
                                                       float temperature, int maxTokens, Exception e) {
        log.warn("Fallback triggered for model {} with custom config. Error type: {}", 
                 model, e.getClass().getSimpleName());
        return GeminiResponseDTO.fallback(
                "I apologize, but I'm temporarily unable to generate a response. Please try again later.",
                model
        );
    }

    // ==================== Helper Methods ====================

    /**
     * Validates that the Gemini API is properly configured.
     */
    private void validateConfiguration() {
        if (!geminiConfig.isConfigured()) {
            throw new AiConfigurationException("Gemini API key is not configured");
        }
        if (geminiClient == null) {
            throw new AiConfigurationException("Gemini client is not initialized");
        }
    }

    /**
     * Validates the prompt is not null or empty.
     */
    private void validatePrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new AiServiceException("Prompt cannot be null or empty", "INVALID_PROMPT", false);
        }
    }

    /**
     * Builds a GenerateContentConfig with custom parameters.
     */
    private GenerateContentConfig buildConfig(float temperature, int maxTokens) {
        return GenerateContentConfig.builder()
                .maxOutputTokens(maxTokens)
                .temperature(temperature)
                .build();
    }

    /**
     * Extracts text content from the Gemini response.
     */
    private String extractText(GenerateContentResponse response) {
        if (response == null) {
            return "";
        }
        
        Optional<List<Candidate>> candidatesOpt = response.candidates();
        if (candidatesOpt.isEmpty() || candidatesOpt.get().isEmpty()) {
            return "";
        }
        
        StringBuilder textBuilder = new StringBuilder();
        for (Candidate candidate : candidatesOpt.get()) {
            Optional<Content> contentOpt = candidate.content();
            if (contentOpt.isPresent() && contentOpt.get().parts().isPresent()) {
                for (Part part : contentOpt.get().parts().get()) {
                    if (part.text().isPresent()) {
                        textBuilder.append(part.text().get());
                    }
                }
            }
        }
        return textBuilder.toString();
    }

    /**
     * Extracts token usage from the response or estimates it.
     */
    private TokenUsageDTO extractTokenUsage(GenerateContentResponse response, String prompt, String output) {
        int inputTokens = estimateTokens(prompt);
        int outputTokens = estimateTokens(output);

        // Try to get actual token counts from response metadata
        Optional<GenerateContentResponseUsageMetadata> metadataOpt = response.usageMetadata();
        if (metadataOpt.isPresent()) {
            GenerateContentResponseUsageMetadata metadata = metadataOpt.get();
            if (metadata.promptTokenCount().isPresent()) {
                inputTokens = metadata.promptTokenCount().get();
            }
            if (metadata.candidatesTokenCount().isPresent()) {
                outputTokens = metadata.candidatesTokenCount().get();
            }
        }

        // Calculate estimated cost
        double cost = (inputTokens * INPUT_COST_PER_MILLION / 1_000_000) 
                    + (outputTokens * OUTPUT_COST_PER_MILLION / 1_000_000);

        return TokenUsageDTO.of(inputTokens, outputTokens, cost);
    }

    /**
     * Extracts the finish reason from the response.
     */
    private String extractFinishReason(GenerateContentResponse response) {
        if (response == null) {
            return "UNKNOWN";
        }
        
        Optional<List<Candidate>> candidatesOpt = response.candidates();
        if (candidatesOpt.isEmpty() || candidatesOpt.get().isEmpty()) {
            return "UNKNOWN";
        }
        
        for (Candidate candidate : candidatesOpt.get()) {
            if (candidate.finishReason().isPresent()) {
                return candidate.finishReason().get().toString();
            }
        }
        return "UNKNOWN";
    }

    /**
     * Maps exceptions to appropriate AI exception types.
     */
    private RuntimeException mapException(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "Unknown error";

        if (message.contains("rate limit") || message.contains("quota")) {
            return new AiRateLimitException("AI rate limit exceeded: " + message, Instant.now().plusSeconds(60));
        }
        if (message.contains("timeout") || message.contains("timed out")) {
            return new AiTimeoutException("AI request timed out: " + message, 10000, "generateContent");
        }
        if (message.contains("authentication") || message.contains("API key")) {
            return new AiConfigurationException("AI authentication failed: " + message);
        }
        
        return new AiServiceException("AI service error: " + message, e);
    }
}
