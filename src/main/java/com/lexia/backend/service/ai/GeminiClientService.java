package com.lexia.backend.service.ai;

import com.lexia.backend.dto.ai.GeminiResponseDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for Google Gemini AI client operations.
 * 
 * <p>
 * Provides methods for:
 * </p>
 * <ul>
 * <li>Synchronous content generation</li>
 * <li>Asynchronous content generation</li>
 * <li>SSE streaming for real-time responses</li>
 * <li>Token estimation for cost tracking</li>
 * </ul>
 * 
 * <p>
 * All methods include resilience patterns (retry, circuit breaker, rate
 * limiting)
 * and fallback mechanisms for graceful degradation.
 * </p>
 * 
 * @see com.lexia.backend.config.GeminiConfig
 */
public interface GeminiClientService {

    /**
     * Generates content synchronously using the default model.
     * 
     * <p>
     * Uses default model (gemini-2.0-flash-exp) with structured output config.
     * Includes retry (3 attempts) and circuit breaker protection.
     * </p>
     * 
     * @param prompt the prompt to send to Gemini
     * @return GeminiResponseDTO containing generated content and metadata
     * @throws com.lexia.backend.exception.ai.AiServiceException       if generation
     *                                                                 fails after
     *                                                                 retries
     * @throws com.lexia.backend.exception.ai.AiConfigurationException if API is not
     *                                                                 configured
     * @throws com.lexia.backend.exception.ai.AiRateLimitException     if rate limit
     *                                                                 is exceeded
     */
    GeminiResponseDTO generateContent(String prompt);

    /**
     * Generates content synchronously using the specified model.
     * 
     * @param prompt the prompt to send to Gemini
     * @param model  the model to use (e.g., "gemini-2.0-flash-exp",
     *               "gemini-1.5-pro")
     * @return GeminiResponseDTO containing generated content and metadata
     * @throws com.lexia.backend.exception.ai.AiServiceException if generation fails
     */
    GeminiResponseDTO generateContent(String prompt, String model);

    /**
     * Generates content synchronously with custom configuration.
     * 
     * @param prompt      the prompt to send to Gemini
     * @param model       the model to use
     * @param temperature creativity level (0.0-1.0)
     * @param maxTokens   maximum output tokens
     * @return GeminiResponseDTO containing generated content and metadata
     */
    GeminiResponseDTO generateContent(String prompt, String model, float temperature, int maxTokens);

    /**
     * Generates structured content (JSON) synchronously.
     * 
     * <p>
     * Sets responseMimeType to "application/json" to ensure valid JSON output.
     * </p>
     * 
     * @param prompt      the prompt to send to Gemini
     * @param model       the model to use
     * @param temperature creativity level (0.0-1.0)
     * @param maxTokens   maximum output tokens
     * @return GeminiResponseDTO containing generated content and metadata
     */
    GeminiResponseDTO generateStructuredContent(String prompt, String model, float temperature, int maxTokens);

    /**
     * Generates structured content (JSON) synchronously using default model and
     * config.
     * 
     * @param prompt the prompt to send to Gemini
     * @return GeminiResponseDTO containing generated content and metadata
     */
    GeminiResponseDTO generateStructuredContent(String prompt);

    /**
     * Generates content asynchronously using the default model.
     * 
     * <p>
     * Non-blocking call that returns immediately with a CompletableFuture.
     * Useful for parallel AI operations.
     * </p>
     * 
     * @param prompt the prompt to send to Gemini
     * @return CompletableFuture that will contain the response when complete
     */
    CompletableFuture<GeminiResponseDTO> generateContentAsync(String prompt);

    /**
     * Generates content asynchronously using the specified model.
     * 
     * @param prompt the prompt to send to Gemini
     * @param model  the model to use
     * @return CompletableFuture that will contain the response when complete
     */
    CompletableFuture<GeminiResponseDTO> generateContentAsync(String prompt, String model);

    /**
     * Streams content generation via Server-Sent Events (SSE).
     * 
     * <p>
     * Returns an SseEmitter that streams tokens as they are generated.
     * Event format:
     * </p>
     * <ul>
     * <li><code>event: token</code> - Individual tokens during generation</li>
     * <li><code>event: complete</code> - Generation finished successfully</li>
     * <li><code>event: error</code> - An error occurred during streaming</li>
     * </ul>
     * 
     * <p>
     * Example SSE events:
     * </p>
     * 
     * <pre>
     * event: token
     * data: {"content": "Hello", "index": 0}
     * 
     * event: token
     * data: {"content": " world", "index": 1}
     * 
     * event: complete
     * data: {"messageId": "uuid", "totalTokens": 150}
     * </pre>
     * 
     * @param prompt the prompt to send to Gemini
     * @return SseEmitter for streaming response
     */
    SseEmitter streamContent(String prompt);

    /**
     * Streams content generation via SSE using the specified model.
     * 
     * @param prompt the prompt to send to Gemini
     * @param model  the model to use
     * @return SseEmitter for streaming response
     */
    SseEmitter streamContent(String prompt, String model);

    /**
     * Generates content with inline file data.
     * Uses Part.fromBytes() for files that cannot be accessed via URL (e.g.,
     * localhost).
     * 
     * <p>
     * This method is useful when the file is stored locally and Gemini cannot
     * access it via URL. The file data is sent directly as part of the request.
     * </p>
     * 
     * <p>
     * Size limit: 20MB total request size (prompt + file data).
     * </p>
     * 
     * @param prompt   the text prompt
     * @param fileData the file content as byte array
     * @param mimeType the MIME type (e.g., "application/pdf", "image/png",
     *                 "image/jpeg")
     * @return GeminiResponseDTO containing generated content and metadata
     * @throws com.lexia.backend.exception.ai.AiServiceException if file data is
     *                                                           null/empty or
     *                                                           exceeds size limit
     */
    GeminiResponseDTO generateContentWithFile(String prompt, byte[] fileData, String mimeType);

    /**
     * Generates content using Gemini URL Context tool.
     * 
     * <p>
     * This method uses the URL Context tool which allows Gemini to fetch and
     * analyze
     * content from web URLs directly. It supports dynamic websites
     * (JavaScript-rendered),
     * PDFs, images, and various text formats.
     * </p>
     * 
     * <p>
     * Supported content types:
     * </p>
     * <ul>
     * <li>HTML (text/html) - including dynamic/SPA websites</li>
     * <li>PDF (application/pdf)</li>
     * <li>Images (image/png, image/jpeg, image/bmp, image/webp)</li>
     * <li>Text (text/plain, application/json, text/xml, text/csv)</li>
     * </ul>
     * 
     * <p>
     * Limitations:
     * </p>
     * <ul>
     * <li>Maximum 20 URLs per request</li>
     * <li>Maximum 34MB content per URL</li>
     * <li>Requires Gemini 2.5 models (gemini-2.5-flash, gemini-2.5-pro, etc.)</li>
     * </ul>
     * 
     * @param prompt the text prompt describing what to extract/analyze
     * @param urls   one or more URLs to fetch content from
     * @return GeminiResponseDTO containing generated content and metadata
     * @throws com.lexia.backend.exception.ai.AiServiceException if URL fetch fails
     *                                                           or content is
     *                                                           blocked
     */
    GeminiResponseDTO generateContentWithUrl(String prompt, String... urls);

    /**
     * Estimates the number of tokens in the given text.
     * 
     * <p>
     * Uses a heuristic approximation (~4 characters per token for English).
     * This is useful for:
     * </p>
     * <ul>
     * <li>Pre-flight cost estimation</li>
     * <li>Prompt truncation to fit token limits</li>
     * <li>Quota tracking</li>
     * </ul>
     * 
     * @param text the text to estimate tokens for
     * @return estimated number of tokens
     */
    int estimateTokens(String text);

    /**
     * Checks if the Gemini API is configured and ready.
     * 
     * @return true if API key is configured and client is initialized
     */
    boolean isConfigured();

    /**
     * Checks the health status of the Gemini service.
     * 
     * @return true if the service is healthy and circuit breaker is closed
     */
    boolean isHealthy();

    /**
     * Gets the current circuit breaker state.
     * 
     * @return circuit breaker state (CLOSED, OPEN, HALF_OPEN)
     */
    String getCircuitBreakerState();
}
