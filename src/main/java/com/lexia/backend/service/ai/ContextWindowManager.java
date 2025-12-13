package com.lexia.backend.service.ai;

import com.lexia.backend.entity.RolePlayConversation;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing conversation context windows.
 * 
 * <p>Implements a sliding window + summarization hybrid strategy to manage
 * token usage in AI conversations. This ensures efficient use of Gemini API
 * tokens while maintaining conversation context.</p>
 * 
 * <h2>Strategy Overview</h2>
 * <ul>
 *   <li><b>Sliding Window</b>: Keep last 10 messages in full</li>
 *   <li><b>Summarization</b>: Messages 11+ summarized via Gemini</li>
 *   <li><b>Token Budget</b>: Max 2000 tokens for context (reserve 2000 for output)</li>
 *   <li><b>Trigger</b>: Summarize when total tokens exceed 3000</li>
 * </ul>
 * 
 * <p>Expected savings: ~60% token reduction on long conversations (20+ messages)</p>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Build context for AI prompt
 * String context = contextWindowManager.buildContextWindow(conversation);
 * 
 * // Check if summarization is needed after adding messages
 * if (contextWindowManager.shouldSummarize(conversation)) {
 *     contextWindowManager.summarizeOldMessages(conversation);
 * }
 * }</pre>
 * 
 * @author LEXIA Team
 * @since Sprint 5 - Task B9
 */
public interface ContextWindowManager {

    /**
     * Default sliding window size (number of recent messages to keep in full).
     */
    int DEFAULT_WINDOW_SIZE = 10;

    /**
     * Maximum token budget for context (input tokens).
     */
    int TOKEN_BUDGET = 2000;

    /**
     * Threshold to trigger summarization (total tokens before summarizing).
     */
    int SUMMARIZATION_TRIGGER = 3000;

    /**
     * Builds the context window for an AI prompt.
     * 
     * <p>Constructs a context string that includes:</p>
     * <ol>
     *   <li>Context summary (if available from previous summarization)</li>
     *   <li>Last N messages (default: 10) in full</li>
     * </ol>
     * 
     * <p>Format example:</p>
     * <pre>
     * [Previous context summary: User discussed project deadlines and budget concerns...]
     * 
     * Conversation:
     * user: Hello
     * ai: Hi there! How can I help?
     * user: I need to discuss the project
     * ai: Of course, what would you like to cover?
     * </pre>
     * 
     * @param conversation the conversation entity
     * @return formatted context string for AI prompt
     */
    String buildContextWindow(RolePlayConversation conversation);

    /**
     * Builds the context window with a custom window size.
     * 
     * @param conversation the conversation entity
     * @param windowSize number of recent messages to include
     * @return formatted context string for AI prompt
     */
    String buildContextWindow(RolePlayConversation conversation, int windowSize);

    /**
     * Checks if the conversation should be summarized.
     * 
     * <p>Returns true if:</p>
     * <ul>
     *   <li>Total estimated tokens exceed {@link #SUMMARIZATION_TRIGGER}</li>
     *   <li>Message count exceeds the sliding window size</li>
     * </ul>
     * 
     * @param conversation the conversation entity
     * @return true if summarization is recommended
     */
    boolean shouldSummarize(RolePlayConversation conversation);

    /**
     * Summarizes old messages and updates the conversation.
     * 
     * <p>This method:</p>
     * <ol>
     *   <li>Extracts messages older than the sliding window</li>
     *   <li>Sends them to Gemini for summarization</li>
     *   <li>Updates the conversation's contextSummary field</li>
     *   <li>Removes the summarized messages (keeps only recent ones)</li>
     * </ol>
     * 
     * <p><b>Note:</b> This modifies the conversation entity but does NOT persist it.
     * Caller must save the conversation after calling this method.</p>
     * 
     * @param conversation the conversation entity (will be modified)
     * @return true if summarization was performed, false if not needed
     */
    boolean summarizeOldMessages(RolePlayConversation conversation);

    /**
     * Estimates the token count for a text string.
     * 
     * <p>Uses a simple heuristic: ~4 characters per token (for English text).
     * This is an approximation that works well for most use cases.</p>
     * 
     * @param text the text to estimate
     * @return estimated token count
     */
    int estimateTokens(String text);

    /**
     * Estimates the total token count for a conversation.
     * 
     * <p>Includes tokens from:</p>
     * <ul>
     *   <li>Context summary (if present)</li>
     *   <li>All messages in the conversation</li>
     * </ul>
     * 
     * @param conversation the conversation entity
     * @return estimated total token count
     */
    int estimateTotalTokens(RolePlayConversation conversation);

    /**
     * Gets the messages within the sliding window.
     * 
     * @param conversation the conversation entity
     * @return list of messages within the window (most recent N messages)
     */
    List<Map<String, Object>> getWindowMessages(RolePlayConversation conversation);

    /**
     * Gets the messages outside the sliding window (candidates for summarization).
     * 
     * @param conversation the conversation entity
     * @return list of messages outside the window (oldest messages)
     */
    List<Map<String, Object>> getOldMessages(RolePlayConversation conversation);

    /**
     * Gets the current sliding window size.
     * 
     * @return the window size
     */
    int getWindowSize();

    /**
     * Sets the sliding window size.
     * 
     * @param size the new window size (minimum 5, maximum 20)
     */
    void setWindowSize(int size);

    /**
     * Formats messages for display or prompt building.
     * 
     * @param messages list of message maps
     * @return formatted string with "role: content" format
     */
    String formatMessages(List<Map<String, Object>> messages);
}
