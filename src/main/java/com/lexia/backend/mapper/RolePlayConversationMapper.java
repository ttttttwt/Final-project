package com.lexia.backend.mapper;

import com.lexia.backend.dto.ai.RolePlayConversationDTO;
import com.lexia.backend.dto.ai.RolePlayMessageDTO;
import com.lexia.backend.entity.RolePlayConversation;
import com.lexia.backend.entity.RolePlayScenario;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mapper utility class for converting between RolePlayConversation entities and DTOs.
 */
public class RolePlayConversationMapper {

    private RolePlayConversationMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static RolePlayConversationDTO toDTO(RolePlayConversation conversation) {
        if (conversation == null) {
            return null;
        }

        UUID scenarioId = conversation.getScenario() != null ? conversation.getScenario().getId() : null;

        return RolePlayConversationDTO.builder()
                .id(conversation.getId())
                .userId(conversation.getUserId())
                .scenarioId(scenarioId)
                .messages(mapMessages(conversation.getMessages()))
                .contextSummary(conversation.getContextSummary())
                .status(conversation.getStatus())
                .mode(conversation.getMode())
                .metrics(conversation.getMetrics())
                .feedbackSummary(conversation.getFeedbackSummary())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    public static RolePlayConversation toEntity(RolePlayConversationDTO dto) {
        if (dto == null) {
            return null;
        }

        RolePlayScenario scenario = null;
        if (dto.getScenarioId() != null) {
            scenario = RolePlayScenario.builder().id(dto.getScenarioId()).build();
        }

        return RolePlayConversation.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .scenario(scenario)
                .messages(unmapMessages(dto.getMessages()))
                .contextSummary(dto.getContextSummary())
                .status(dto.getStatus())
                .mode(dto.getMode())
                .metrics(dto.getMetrics())
                .feedbackSummary(dto.getFeedbackSummary())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    private static List<RolePlayMessageDTO> mapMessages(List<Map<String, Object>> raw) {
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }

        List<RolePlayMessageDTO> result = new ArrayList<>(raw.size());
        for (Map<String, Object> item : raw) {
            if (item == null) {
                continue;
            }
            result.add(RolePlayMessageDTO.builder()
                    .role(asString(item.get("role")))
                    .content(asString(item.get("content")))
                    .timestamp(asInstant(item.get("timestamp")))
                    .feedback(asMap(item.get("feedback")))
                    .build());
        }
        return result;
    }

    private static List<Map<String, Object>> unmapMessages(List<RolePlayMessageDTO> raw) {
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> result = new ArrayList<>(raw.size());
        for (RolePlayMessageDTO message : raw) {
            if (message == null) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("role", message.getRole());
            map.put("content", message.getContent());
            map.put("timestamp", message.getTimestamp() != null ? message.getTimestamp().toString() : null);
            if (message.getFeedback() != null && !message.getFeedback().isEmpty()) {
                map.put("feedback", new HashMap<>(message.getFeedback()));
            }
            result.add(map);
        }
        return result;
    }

    private static Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new HashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (e.getKey() != null) {
                    result.put(String.valueOf(e.getKey()), e.getValue());
                }
            }
            return result;
        }
        return null;
    }

    private static Instant asInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof String s) {
            try {
                return Instant.parse(s);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
