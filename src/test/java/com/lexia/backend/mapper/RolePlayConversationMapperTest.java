package com.lexia.backend.mapper;

import com.lexia.backend.dto.ai.RolePlayConversationDTO;
import com.lexia.backend.dto.ai.RolePlayMessageDTO;
import com.lexia.backend.entity.RolePlayConversation;
import com.lexia.backend.entity.RolePlayScenario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RolePlayConversationMapper Unit Tests")
class RolePlayConversationMapperTest {

    @Test
    @DisplayName("Should convert RolePlayConversation entity to DTO with messages")
    void testToDTO_WithMessages_Success() {
        UUID conversationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID scenarioId = UUID.randomUUID();

        Map<String, Object> feedback = new HashMap<>();
        feedback.put("grammar", "Consider using present perfect.");

        Map<String, Object> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", "I finished the tasks yesterday");
        msg.put("timestamp", "2025-12-12T10:00:00Z");
        msg.put("feedback", feedback);

        RolePlayConversation conversation = RolePlayConversation.builder()
                .id(conversationId)
                .userId(userId)
                .scenario(RolePlayScenario.builder().id(scenarioId).build())
                .messages(List.of(msg))
                .status(RolePlayConversation.STATUS_IN_PROGRESS)
                .mode(RolePlayConversation.MODE_LEARNING)
                .build();

        RolePlayConversationDTO dto = RolePlayConversationMapper.toDTO(conversation);

        assertNotNull(dto);
        assertEquals(conversationId, dto.getId());
        assertEquals(userId, dto.getUserId());
        assertEquals(scenarioId, dto.getScenarioId());
        assertEquals("learning", dto.getMode());
        assertEquals(1, dto.getMessages().size());
        assertEquals("user", dto.getMessages().get(0).getRole());
        assertEquals(Instant.parse("2025-12-12T10:00:00Z"), dto.getMessages().get(0).getTimestamp());
        assertNotNull(dto.getMessages().get(0).getFeedback());
        assertEquals("Consider using present perfect.", dto.getMessages().get(0).getFeedback().get("grammar"));
    }

    @Test
    @DisplayName("Should convert DTO back to entity")
    void testToEntity_FromDTO_Success() {
        UUID scenarioId = UUID.randomUUID();

        RolePlayConversationDTO dto = RolePlayConversationDTO.builder()
                .userId(UUID.randomUUID())
                .scenarioId(scenarioId)
                .messages(List.of(RolePlayMessageDTO.builder().role("ai").content("Hello").timestamp(Instant.parse("2025-12-12T11:00:00Z")).build()))
                .status(RolePlayConversation.STATUS_IN_PROGRESS)
                .mode(RolePlayConversation.MODE_IMMERSIVE)
                .build();

        RolePlayConversation entity = RolePlayConversationMapper.toEntity(dto);

        assertNotNull(entity);
        assertNotNull(entity.getScenario());
        assertEquals(scenarioId, entity.getScenario().getId());
        assertEquals(1, entity.getMessages().size());
        assertEquals("ai", String.valueOf(entity.getMessages().get(0).get("role")));
        assertEquals("2025-12-12T11:00:00Z", String.valueOf(entity.getMessages().get(0).get("timestamp")));
    }

    @Test
    @DisplayName("Should return null when mapping null")
    void testNullHandling() {
        assertNull(RolePlayConversationMapper.toDTO(null));
        assertNull(RolePlayConversationMapper.toEntity(null));
    }

    @Test
    @DisplayName("DTO->Entity should create mutable empty message list when null")
    void testToEntity_NullMessages_CreatesMutableList() {
        RolePlayConversationDTO dto = RolePlayConversationDTO.builder()
                .userId(UUID.randomUUID())
                .messages(null)
                .status(RolePlayConversation.STATUS_IN_PROGRESS)
                .mode(RolePlayConversation.MODE_LEARNING)
                .build();

        RolePlayConversation entity = RolePlayConversationMapper.toEntity(dto);

        assertNotNull(entity);
        assertNotNull(entity.getMessages());
        assertEquals(0, entity.getMessages().size());
        assertDoesNotThrow(() -> entity.getMessages().add(Map.of("role", "user", "content", "hi")));
    }

    @Test
    @DisplayName("Entity->DTO should map invalid timestamp string to null")
    void testToDTO_InvalidTimestamp_MapsToNull() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", "hello");
        msg.put("timestamp", "not-a-timestamp");

        RolePlayConversation conversation = RolePlayConversation.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .messages(List.of(msg))
                .status(RolePlayConversation.STATUS_IN_PROGRESS)
                .mode(RolePlayConversation.MODE_LEARNING)
                .build();

        RolePlayConversationDTO dto = RolePlayConversationMapper.toDTO(conversation);

        assertNotNull(dto);
        assertEquals(1, dto.getMessages().size());
        assertNull(dto.getMessages().get(0).getTimestamp());
    }
}
