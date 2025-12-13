package com.lexia.backend.service.ai.impl;

import com.lexia.backend.dto.ai.GeminiResponseDTO;
import com.lexia.backend.entity.RolePlayConversation;
import com.lexia.backend.service.ai.ContextWindowManager;
import com.lexia.backend.service.ai.GeminiClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of ContextWindowManager using sliding window + summarization hybrid.
 * 
 * <p>Manages conversation context to stay within token budgets while preserving
 * important conversation history. Uses Gemini API for summarization.</p>
 * 
 * <h2>Token Estimation</h2>
 * <p>Uses a heuristic of ~4 characters per token, which works well for English text.
 * This matches OpenAI/Gemini's typical tokenization for natural language.</p>
 * 
 * @author LEXIA Team
 * @since Sprint 5 - Task B9
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContextWindowManagerImpl implements ContextWindowManager {

    private final GeminiClientService geminiClientService;

    private static final String SUMMARIZATION_PROMPT = """
            Summarize the following conversation history in 2-3 sentences.
            Focus on key topics discussed, decisions made, and important context.
            Keep the summary concise but informative.
            
            Conversation:
            %s
            
            Summary:
            """;

    private static final double CHARS_PER_TOKEN = 4.0;
    private static final int MIN_WINDOW_SIZE = 5;
    private static final int MAX_WINDOW_SIZE = 20;

    private int windowSize = DEFAULT_WINDOW_SIZE;

    @Override
    public String buildContextWindow(RolePlayConversation conversation) {
        return buildContextWindow(conversation, windowSize);
    }

    @Override
    public String buildContextWindow(RolePlayConversation conversation, int customWindowSize) {
        log.debug("Building context window for conversation {} with window size {}", 
                conversation.getId(), customWindowSize);

        StringBuilder contextBuilder = new StringBuilder();

        // Add context summary if available
        String contextSummary = conversation.getContextSummary();
        if (contextSummary != null && !contextSummary.isBlank()) {
            contextBuilder.append("[Previous context summary: ")
                    .append(contextSummary)
                    .append("]\n\n");
            log.debug("Including context summary ({} chars)", contextSummary.length());
        }

        // Add recent messages within the window
        List<Map<String, Object>> windowMessages = getWindowMessages(conversation, customWindowSize);
        if (!windowMessages.isEmpty()) {
            contextBuilder.append("Conversation:\n");
            contextBuilder.append(formatMessages(windowMessages));
        }

        String context = contextBuilder.toString();
        int estimatedTokens = estimateTokens(context);
        
        log.debug("Built context window: {} chars, ~{} tokens, {} messages", 
                context.length(), estimatedTokens, windowMessages.size());

        return context;
    }

    @Override
    public boolean shouldSummarize(RolePlayConversation conversation) {
        List<Map<String, Object>> messages = conversation.getMessages();
        if (messages == null) {
            return false;
        }

        // Check if we have messages outside the window
        if (messages.size() <= windowSize) {
            return false;
        }

        // Check token threshold
        int totalTokens = estimateTotalTokens(conversation);
        boolean shouldSummarize = totalTokens > SUMMARIZATION_TRIGGER;

        if (shouldSummarize) {
            log.info("Conversation {} requires summarization: {} messages, ~{} tokens (threshold: {})", 
                    conversation.getId(), messages.size(), totalTokens, SUMMARIZATION_TRIGGER);
        }

        return shouldSummarize;
    }

    @Override
    public boolean summarizeOldMessages(RolePlayConversation conversation) {
        List<Map<String, Object>> messages = conversation.getMessages();
        if (messages == null || messages.size() <= windowSize) {
            log.debug("No messages to summarize for conversation {}", conversation.getId());
            return false;
        }

        List<Map<String, Object>> oldMessages = getOldMessages(conversation);
        if (oldMessages.isEmpty()) {
            return false;
        }

        log.info("Summarizing {} old messages for conversation {}", 
                oldMessages.size(), conversation.getId());

        try {
            // Build summarization prompt
            String messagesToSummarize = formatMessages(oldMessages);
            
            // Include existing summary if present
            String existingSummary = conversation.getContextSummary();
            String promptContent = messagesToSummarize;
            if (existingSummary != null && !existingSummary.isBlank()) {
                promptContent = "Previous summary: " + existingSummary + "\n\nNew messages:\n" + messagesToSummarize;
            }

            String prompt = String.format(SUMMARIZATION_PROMPT, promptContent);

            // Call Gemini for summarization
            GeminiResponseDTO response = geminiClientService.generateContent(prompt);
            String newSummary = response.content().trim();

            // Update conversation's context summary only
            // IMPORTANT: Do NOT remove old messages - they are user data and must be preserved
            // The summary is used only for building the prompt context window, not for data storage
            conversation.setContextSummary(newSummary);

            // Calculate token savings for logging (conceptual savings in prompt size)
            int tokensSaved = estimateTokens(messagesToSummarize) - estimateTokens(newSummary);
            log.info("Summarization complete for conversation {}. Summarized {} old messages. " +
                    "Context window now uses summary (~{} tokens saved). Summary: '{}'", 
                    conversation.getId(), 
                    oldMessages.size(),
                    tokensSaved,
                    newSummary.substring(0, Math.min(100, newSummary.length())) + "...");

            return true;

        } catch (Exception e) {
            log.error("Failed to summarize messages for conversation {}: {}", 
                    conversation.getId(), e.getMessage());
            // Don't fail the operation, just skip summarization
            return false;
        }
    }

    @Override
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return (int) Math.ceil(text.length() / CHARS_PER_TOKEN);
    }

    @Override
    public int estimateTotalTokens(RolePlayConversation conversation) {
        int totalTokens = 0;

        // Add context summary tokens
        String contextSummary = conversation.getContextSummary();
        if (contextSummary != null) {
            totalTokens += estimateTokens(contextSummary);
        }

        // Add all message tokens
        List<Map<String, Object>> messages = conversation.getMessages();
        if (messages != null) {
            for (Map<String, Object> msg : messages) {
                Object content = msg.get("content");
                if (content != null) {
                    totalTokens += estimateTokens(content.toString());
                }
            }
        }

        return totalTokens;
    }

    @Override
    public List<Map<String, Object>> getWindowMessages(RolePlayConversation conversation) {
        return getWindowMessages(conversation, windowSize);
    }

    private List<Map<String, Object>> getWindowMessages(RolePlayConversation conversation, int customWindowSize) {
        List<Map<String, Object>> messages = conversation.getMessages();
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }

        int start = Math.max(0, messages.size() - customWindowSize);
        return new ArrayList<>(messages.subList(start, messages.size()));
    }

    @Override
    public List<Map<String, Object>> getOldMessages(RolePlayConversation conversation) {
        List<Map<String, Object>> messages = conversation.getMessages();
        if (messages == null || messages.size() <= windowSize) {
            return new ArrayList<>();
        }

        int end = messages.size() - windowSize;
        return new ArrayList<>(messages.subList(0, end));
    }

    @Override
    public int getWindowSize() {
        return windowSize;
    }

    @Override
    public void setWindowSize(int size) {
        if (size < MIN_WINDOW_SIZE) {
            log.warn("Window size {} below minimum, using {}", size, MIN_WINDOW_SIZE);
            this.windowSize = MIN_WINDOW_SIZE;
        } else if (size > MAX_WINDOW_SIZE) {
            log.warn("Window size {} above maximum, using {}", size, MAX_WINDOW_SIZE);
            this.windowSize = MAX_WINDOW_SIZE;
        } else {
            this.windowSize = size;
        }
        log.info("Context window size set to {}", this.windowSize);
    }

    @Override
    public String formatMessages(List<Map<String, Object>> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }

        return messages.stream()
                .map(msg -> {
                    String role = String.valueOf(msg.getOrDefault("role", "unknown"));
                    String content = String.valueOf(msg.getOrDefault("content", ""));
                    return role + ": " + content;
                })
                .collect(Collectors.joining("\n"));
    }
}
