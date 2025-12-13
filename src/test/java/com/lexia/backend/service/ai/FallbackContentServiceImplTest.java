package com.lexia.backend.service.ai;

import com.lexia.backend.dto.ai.RolePlayScenarioDTO;
import com.lexia.backend.entity.RolePlayScenario;
import com.lexia.backend.mapper.RolePlayScenarioMapper;
import com.lexia.backend.repository.RolePlayScenarioRepository;
import com.lexia.backend.service.ai.impl.FallbackContentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FallbackContentService implementation.
 * 
 * Tests B7: Fallback content for role-play scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FallbackContentService Tests")
class FallbackContentServiceImplTest {

    @Mock
    private RolePlayScenarioRepository scenarioRepository;

    @InjectMocks
    private FallbackContentServiceImpl fallbackContentService;

    private RolePlayScenario sampleFallbackScenario;

    @BeforeEach
    void setUp() {
        sampleFallbackScenario = RolePlayScenario.builder()
                .id(UUID.randomUUID())
                .title("Sample Fallback Scenario")
                .context("You are in a business meeting")
                .yourRole("Sales Manager")
                .aiRole("Client Representative")
                .cefrLevel("B1")
                .domain("meetings")
                .objectives(List.of("Practice negotiation", "Use formal language"))
                .keyVocabulary(List.of(
                        Map.of("term", "negotiate", "definition", "discuss terms", "ipa", "/nɪˈɡəʊʃieɪt/")
                ))
                .openingLine("Good morning! Thank you for meeting with me today.")
                .suggestedDuration(10)
                .isFallback(true)
                .build();
    }

    // ========== Random Fallback Scenario Tests ==========

    @Nested
    @DisplayName("getRandomFallbackScenario")
    class GetRandomFallbackScenarioTests {

        @Test
        @DisplayName("should return scenario when exact match found")
        void shouldReturnScenarioWhenExactMatchFound() {
            // Given
            when(scenarioRepository.findRandomFallbackByCefrLevelAndDomain("B1", "meetings"))
                    .thenReturn(Optional.of(sampleFallbackScenario));

            // When
            Optional<RolePlayScenarioDTO> result = fallbackContentService
                    .getRandomFallbackScenario("B1", "meetings");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getTitle()).isEqualTo("Sample Fallback Scenario");
            assertThat(result.get().getCefrLevel()).isEqualTo("B1");
            assertThat(result.get().getDomain()).isEqualTo("meetings");
            verify(scenarioRepository).findRandomFallbackByCefrLevelAndDomain("B1", "meetings");
        }

        @Test
        @DisplayName("should return empty when no match found")
        void shouldReturnEmptyWhenNoMatchFound() {
            // Given
            when(scenarioRepository.findRandomFallbackByCefrLevelAndDomain("C2", "legal"))
                    .thenReturn(Optional.empty());

            // When
            Optional<RolePlayScenarioDTO> result = fallbackContentService
                    .getRandomFallbackScenario("C2", "legal");

            // Then
            assertThat(result).isEmpty();
            verify(scenarioRepository).findRandomFallbackByCefrLevelAndDomain("C2", "legal");
        }

        @ParameterizedTest
        @CsvSource({
                "A1, meetings",
                "A2, presentations",
                "B1, negotiations",
                "B2, interviews",
                "C1, conferences",
                "C2, legal"
        })
        @DisplayName("should query with correct CEFR level and domain combinations")
        void shouldQueryWithCorrectParameters(String cefrLevel, String domain) {
            // Given
            when(scenarioRepository.findRandomFallbackByCefrLevelAndDomain(cefrLevel, domain))
                    .thenReturn(Optional.empty());

            // When
            fallbackContentService.getRandomFallbackScenario(cefrLevel, domain);

            // Then
            verify(scenarioRepository).findRandomFallbackByCefrLevelAndDomain(cefrLevel, domain);
        }
    }

    // ========== Flexible Fallback Scenario Tests ==========

    @Nested
    @DisplayName("getRandomFallbackScenarioFlexible")
    class GetRandomFallbackScenarioFlexibleTests {

        @Test
        @DisplayName("should return exact match when available")
        void shouldReturnExactMatchWhenAvailable() {
            // Given
            when(scenarioRepository.findRandomFallbackByCefrLevelAndDomain("B1", "meetings"))
                    .thenReturn(Optional.of(sampleFallbackScenario));

            // When
            Optional<RolePlayScenarioDTO> result = fallbackContentService
                    .getRandomFallbackScenarioFlexible("B1", "meetings");

            // Then
            assertThat(result).isPresent();
            verify(scenarioRepository).findRandomFallbackByCefrLevelAndDomain("B1", "meetings");
            verify(scenarioRepository, never()).findRandomFallbackByCefrLevel(anyString());
            verify(scenarioRepository, never()).findRandomFallbackByDomain(anyString());
        }

        @Test
        @DisplayName("should fallback to CEFR level only when exact match not found")
        void shouldFallbackToCefrLevelOnly() {
            // Given
            when(scenarioRepository.findRandomFallbackByCefrLevelAndDomain("B1", "meetings"))
                    .thenReturn(Optional.empty());
            when(scenarioRepository.findRandomFallbackByCefrLevel("B1"))
                    .thenReturn(Optional.of(sampleFallbackScenario));

            // When
            Optional<RolePlayScenarioDTO> result = fallbackContentService
                    .getRandomFallbackScenarioFlexible("B1", "meetings");

            // Then
            assertThat(result).isPresent();
            verify(scenarioRepository).findRandomFallbackByCefrLevelAndDomain("B1", "meetings");
            verify(scenarioRepository).findRandomFallbackByCefrLevel("B1");
            verify(scenarioRepository, never()).findRandomFallbackByDomain(anyString());
        }

        @Test
        @DisplayName("should fallback to domain only when CEFR level not found")
        void shouldFallbackToDomainOnly() {
            // Given
            when(scenarioRepository.findRandomFallbackByCefrLevelAndDomain("B1", "meetings"))
                    .thenReturn(Optional.empty());
            when(scenarioRepository.findRandomFallbackByCefrLevel("B1"))
                    .thenReturn(Optional.empty());
            when(scenarioRepository.findRandomFallbackByDomain("meetings"))
                    .thenReturn(Optional.of(sampleFallbackScenario));

            // When
            Optional<RolePlayScenarioDTO> result = fallbackContentService
                    .getRandomFallbackScenarioFlexible("B1", "meetings");

            // Then
            assertThat(result).isPresent();
            verify(scenarioRepository).findRandomFallbackByCefrLevelAndDomain("B1", "meetings");
            verify(scenarioRepository).findRandomFallbackByCefrLevel("B1");
            verify(scenarioRepository).findRandomFallbackByDomain("meetings");
        }

        @Test
        @DisplayName("should fallback to any scenario when no specific match found")
        void shouldFallbackToAnyScenario() {
            // Given
            when(scenarioRepository.findRandomFallbackByCefrLevelAndDomain("B1", "meetings"))
                    .thenReturn(Optional.empty());
            when(scenarioRepository.findRandomFallbackByCefrLevel("B1"))
                    .thenReturn(Optional.empty());
            when(scenarioRepository.findRandomFallbackByDomain("meetings"))
                    .thenReturn(Optional.empty());
            when(scenarioRepository.findRandomFallback())
                    .thenReturn(Optional.of(sampleFallbackScenario));

            // When
            Optional<RolePlayScenarioDTO> result = fallbackContentService
                    .getRandomFallbackScenarioFlexible("B1", "meetings");

            // Then
            assertThat(result).isPresent();
            verify(scenarioRepository).findRandomFallback();
        }

        @Test
        @DisplayName("should return empty when no fallback exists")
        void shouldReturnEmptyWhenNoFallbackExists() {
            // Given
            when(scenarioRepository.findRandomFallbackByCefrLevelAndDomain("B1", "meetings"))
                    .thenReturn(Optional.empty());
            when(scenarioRepository.findRandomFallbackByCefrLevel("B1"))
                    .thenReturn(Optional.empty());
            when(scenarioRepository.findRandomFallbackByDomain("meetings"))
                    .thenReturn(Optional.empty());
            when(scenarioRepository.findRandomFallback())
                    .thenReturn(Optional.empty());

            // When
            Optional<RolePlayScenarioDTO> result = fallbackContentService
                    .getRandomFallbackScenarioFlexible("B1", "meetings");

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ========== Get All Fallback Scenarios Tests ==========

    @Nested
    @DisplayName("getFallbackScenarios")
    class GetFallbackScenariosTests {

        @Test
        @DisplayName("should return scenarios matching CEFR level and domain")
        void shouldReturnScenariosMatchingBothCriteria() {
            // Given
            List<RolePlayScenario> scenarios = List.of(sampleFallbackScenario);
            when(scenarioRepository.findByIsFallbackTrueAndCefrLevelAndDomain("B1", "meetings"))
                    .thenReturn(scenarios);

            // When
            List<RolePlayScenarioDTO> result = fallbackContentService
                    .getFallbackScenarios("B1", "meetings");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Sample Fallback Scenario");
        }

        @Test
        @DisplayName("should return scenarios matching CEFR level only")
        void shouldReturnScenariosMatchingCefrLevelOnly() {
            // Given
            List<RolePlayScenario> scenarios = List.of(sampleFallbackScenario);
            when(scenarioRepository.findByIsFallbackTrueAndCefrLevel("B1"))
                    .thenReturn(scenarios);

            // When
            List<RolePlayScenarioDTO> result = fallbackContentService
                    .getFallbackScenarios("B1", null);

            // Then
            assertThat(result).hasSize(1);
            verify(scenarioRepository).findByIsFallbackTrueAndCefrLevel("B1");
        }

        @Test
        @DisplayName("should return scenarios matching domain only")
        void shouldReturnScenariosMatchingDomainOnly() {
            // Given
            List<RolePlayScenario> scenarios = List.of(sampleFallbackScenario);
            when(scenarioRepository.findByIsFallbackTrueAndDomain("meetings"))
                    .thenReturn(scenarios);

            // When
            List<RolePlayScenarioDTO> result = fallbackContentService
                    .getFallbackScenarios(null, "meetings");

            // Then
            assertThat(result).hasSize(1);
            verify(scenarioRepository).findByIsFallbackTrueAndDomain("meetings");
        }

        @Test
        @DisplayName("should return all fallback scenarios when no criteria")
        void shouldReturnAllFallbackScenariosWhenNoCriteria() {
            // Given
            List<RolePlayScenario> scenarios = List.of(sampleFallbackScenario);
            when(scenarioRepository.findByIsFallbackTrue())
                    .thenReturn(scenarios);

            // When
            List<RolePlayScenarioDTO> result = fallbackContentService
                    .getFallbackScenarios(null, null);

            // Then
            assertThat(result).hasSize(1);
            verify(scenarioRepository).findByIsFallbackTrue();
        }

        @Test
        @DisplayName("should return empty list when no scenarios found")
        void shouldReturnEmptyListWhenNoScenariosFound() {
            // Given
            when(scenarioRepository.findByIsFallbackTrueAndCefrLevelAndDomain("C2", "legal"))
                    .thenReturn(Collections.emptyList());

            // When
            List<RolePlayScenarioDTO> result = fallbackContentService
                    .getFallbackScenarios("C2", "legal");

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ========== Count and Has Fallback Scenarios Tests ==========

    @Nested
    @DisplayName("countFallbackScenarios")
    class CountFallbackScenariosTests {

        @Test
        @DisplayName("should count scenarios by CEFR level and domain")
        void shouldCountByBothCriteria() {
            // Given
            when(scenarioRepository.countByIsFallbackTrueAndCefrLevelAndDomain("B1", "meetings"))
                    .thenReturn(5L);

            // When
            long count = fallbackContentService.countFallbackScenarios("B1", "meetings");

            // Then
            assertThat(count).isEqualTo(5L);
        }

        @Test
        @DisplayName("should count scenarios by CEFR level only")
        void shouldCountByCefrLevelOnly() {
            // Given
            when(scenarioRepository.countByIsFallbackTrueAndCefrLevel("B1"))
                    .thenReturn(10L);

            // When
            long count = fallbackContentService.countFallbackScenarios("B1", null);

            // Then
            assertThat(count).isEqualTo(10L);
        }

        @Test
        @DisplayName("should count scenarios by domain only")
        void shouldCountByDomainOnly() {
            // Given
            when(scenarioRepository.countByIsFallbackTrueAndDomain("meetings"))
                    .thenReturn(8L);

            // When
            long count = fallbackContentService.countFallbackScenarios(null, "meetings");

            // Then
            assertThat(count).isEqualTo(8L);
        }

        @Test
        @DisplayName("should count all fallback scenarios when no criteria")
        void shouldCountAllWhenNoCriteria() {
            // Given
            when(scenarioRepository.countByIsFallbackTrue())
                    .thenReturn(50L);

            // When
            long count = fallbackContentService.countFallbackScenarios(null, null);

            // Then
            assertThat(count).isEqualTo(50L);
        }
    }

    @Nested
    @DisplayName("hasFallbackScenarios")
    class HasFallbackScenariosTests {

        @Test
        @DisplayName("should return true when scenarios exist for criteria")
        void shouldReturnTrueWhenScenariosExist() {
            // Given
            when(scenarioRepository.existsByIsFallbackTrueAndCefrLevelAndDomain("B1", "meetings"))
                    .thenReturn(true);

            // When
            boolean result = fallbackContentService.hasFallbackScenarios("B1", "meetings");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when no scenarios exist")
        void shouldReturnFalseWhenNoScenariosExist() {
            // Given
            when(scenarioRepository.existsByIsFallbackTrueAndCefrLevelAndDomain("C2", "legal"))
                    .thenReturn(false);

            // When
            boolean result = fallbackContentService.hasFallbackScenarios("C2", "legal");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should use count when CEFR level is null")
        void shouldUseCountWhenCefrLevelIsNull() {
            // Given
            when(scenarioRepository.countByIsFallbackTrueAndDomain("meetings"))
                    .thenReturn(5L);

            // When
            boolean result = fallbackContentService.hasFallbackScenarios(null, "meetings");

            // Then
            assertThat(result).isTrue();
            verify(scenarioRepository).countByIsFallbackTrueAndDomain("meetings");
        }
    }

    // ========== Generate Fallback Response Tests ==========

    @Nested
    @DisplayName("generateFallbackResponse")
    class GenerateFallbackResponseTests {

        @ParameterizedTest
        @ValueSource(strings = {"A1", "A2"})
        @DisplayName("should generate A1-A2 level response")
        void shouldGenerateA1A2LevelResponse(String level) {
            // When
            String response = fallbackContentService
                    .generateFallbackResponse(level, "Manager", "Hello");

            // Then
            assertThat(response)
                    .isNotBlank()
                    .hasSizeLessThan(100); // A1-A2 responses should be short
        }

        @ParameterizedTest
        @ValueSource(strings = {"B1", "B2"})
        @DisplayName("should generate B1-B2 level response")
        void shouldGenerateB1B2LevelResponse(String level) {
            // When
            String response = fallbackContentService
                    .generateFallbackResponse(level, "Manager", "Hello");

            // Then
            assertThat(response)
                    .isNotBlank()
                    .contains("?"); // B1-B2 responses often ask questions
        }

        @ParameterizedTest
        @ValueSource(strings = {"C1", "C2"})
        @DisplayName("should generate C1-C2 level response")
        void shouldGenerateC1C2LevelResponse(String level) {
            // When
            String response = fallbackContentService
                    .generateFallbackResponse(level, "Manager", "Hello");

            // Then
            assertThat(response)
                    .isNotBlank()
                    .hasSizeGreaterThan(50); // C1-C2 responses should be more elaborate
        }

        @Test
        @DisplayName("should default to B1-B2 level for null CEFR")
        void shouldDefaultToB1B2ForNullCefr() {
            // When
            String response = fallbackContentService
                    .generateFallbackResponse(null, "Manager", "Hello");

            // Then
            assertThat(response).isNotBlank();
        }

        @Test
        @DisplayName("should default to B1-B2 level for unknown CEFR")
        void shouldDefaultToB1B2ForUnknownCefr() {
            // When
            String response = fallbackContentService
                    .generateFallbackResponse("X1", "Manager", "Hello");

            // Then
            assertThat(response).isNotBlank();
        }
    }

    // ========== Generate Fallback Learning Response Tests ==========

    @Nested
    @DisplayName("generateFallbackLearningResponse")
    class GenerateFallbackLearningResponseTests {

        @Test
        @DisplayName("should generate valid JSON response")
        void shouldGenerateValidJsonResponse() {
            // When
            String response = fallbackContentService
                    .generateFallbackLearningResponse("B1", "Manager", "Hello");

            // Then
            assertThat(response)
                    .contains("\"aiMessage\"")
                    .contains("\"feedback\"")
                    .contains("\"grammarFeedback\"")
                    .contains("\"vocabularyFeedback\"")
                    .contains("\"fluencyScore\"")
                    .contains("\"suggestions\"");
        }

        @Test
        @DisplayName("should include placeholder feedback values")
        void shouldIncludePlaceholderFeedbackValues() {
            // When
            String response = fallbackContentService
                    .generateFallbackLearningResponse("B1", "Manager", "Hello");

            // Then
            assertThat(response)
                    .contains("Keep practicing")
                    .contains("Good vocabulary");
        }

        @ParameterizedTest
        @ValueSource(strings = {"A1", "B1", "C1"})
        @DisplayName("should work for all CEFR levels")
        void shouldWorkForAllCefrLevels(String level) {
            // When
            String response = fallbackContentService
                    .generateFallbackLearningResponse(level, "Manager", "Test message");

            // Then
            assertThat(response)
                    .isNotBlank()
                    .contains("aiMessage");
        }
    }
}
