package com.lexia.backend.mapper;

import com.lexia.backend.dto.ai.RolePlayScenarioDTO;
import com.lexia.backend.dto.ai.RolePlayVocabularyItemDTO;
import com.lexia.backend.entity.RolePlayScenario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RolePlayScenarioMapper Unit Tests")
class RolePlayScenarioMapperTest {

    @Test
    @DisplayName("Should convert RolePlayScenario entity to DTO with vocabulary")
    void testToDTO_WithAllFields_Success() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2025-12-12T10:15:30Z");

        RolePlayScenario scenario = RolePlayScenario.builder()
                .id(id)
                .title("Project Status Meeting")
                .context("You are in a weekly team meeting")
                .yourRole("Team Member")
                .aiRole("Project Manager")
                .cefrLevel("B1")
                .domain("meetings")
                .industry("technology")
                .objectives(List.of("Report progress", "Discuss blockers"))
                .keyVocabulary(List.of(RolePlayScenario.vocabItem("deadline", "finish date", "The deadline is Friday.")))
                .openingLine("Good morning! Let's start.")
                .suggestedDuration(10)
                .isFallback(false)
                .createdAt(now)
                .build();

        RolePlayScenarioDTO dto = RolePlayScenarioMapper.toDTO(scenario);

        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals("Project Status Meeting", dto.getTitle());
        assertEquals("B1", dto.getCefrLevel());
        assertEquals(2, dto.getObjectives().size());
        assertEquals(1, dto.getKeyVocabulary().size());
        assertEquals("deadline", dto.getKeyVocabulary().get(0).getTerm());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    @DisplayName("Should convert DTO back to entity")
    void testToEntity_FromDTO_Success() {
        UUID id = UUID.randomUUID();
        RolePlayScenarioDTO dto = RolePlayScenarioDTO.builder()
                .id(id)
                .title("First Day at Work")
                .context("You are starting a new job")
                .yourRole("New Employee")
                .aiRole("Colleague")
                .cefrLevel("A2")
                .domain("workplace")
                .objectives(List.of("Introduce yourself"))
                .keyVocabulary(List.of(RolePlayVocabularyItemDTO.builder().term("colleague").definition("person you work with").example("My colleague is friendly.").build()))
                .openingLine("Welcome!")
                .suggestedDuration(5)
                .isFallback(true)
                .build();

        RolePlayScenario entity = RolePlayScenarioMapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertEquals("A2", entity.getCefrLevel());
        assertNotNull(entity.getKeyVocabulary());
        assertEquals(1, entity.getKeyVocabulary().size());
        assertEquals("colleague", String.valueOf(entity.getKeyVocabulary().get(0).get("term")));
    }

    @Test
    @DisplayName("Should return null when mapping null")
    void testNullHandling() {
        assertNull(RolePlayScenarioMapper.toDTO(null));
        assertNull(RolePlayScenarioMapper.toEntity(null));
    }

    @Test
    @DisplayName("DTO->Entity should defensively copy objectives into a mutable list")
    void testToEntity_Objectives_DefensiveCopyAndMutable() {
        RolePlayScenarioDTO dto = RolePlayScenarioDTO.builder()
                .id(UUID.randomUUID())
                .title("Test")
                .cefrLevel("A2")
                .domain("workplace")
                .objectives(List.of("Introduce yourself"))
                .keyVocabulary(null)
                .build();

        RolePlayScenario entity = RolePlayScenarioMapper.toEntity(dto);

        assertNotNull(entity);
        assertNotNull(entity.getObjectives());
        assertEquals(1, entity.getObjectives().size());
        assertDoesNotThrow(() -> entity.getObjectives().add("Ask for help"));

        assertNotNull(entity.getKeyVocabulary());
        assertDoesNotThrow(() -> entity.getKeyVocabulary().add(RolePlayScenario.vocabItem("term", "def", "ex")));
    }
}
