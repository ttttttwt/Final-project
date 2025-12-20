package com.lexia.backend.service.ai;

import java.util.UUID;

/**
 * Service interface for processing custom materials asynchronously.
 * 
 * <p>
 * Orchestrates the content extraction and AI generation pipeline.
 * </p>
 * 
 * @since Sprint 5
 */
public interface CustomMaterialProcessingService {

    /**
     * Processes a material: extracts content and generates learning materials.
     * 
     * <p>
     * This is the main entry point for async processing. It will:
     * </p>
     * <ol>
     * <li>Extract content from the source</li>
     * <li>Generate vocabulary, quiz, summary, etc. based on settings</li>
     * <li>Save results to the material entity</li>
     * <li>Update job status and progress</li>
     * </ol>
     * 
     * @param materialId The material ID to process
     */
    void processMaterial(UUID materialId);

    /**
     * Retries processing for a failed material.
     * 
     * @param materialId The material ID to retry
     * @return true if retry was started, false if max retries exceeded
     */
    boolean retryProcessing(UUID materialId);

    /**
     * Cancels processing for a material.
     * 
     * @param materialId The material ID to cancel
     */
    void cancelProcessing(UUID materialId);
}
