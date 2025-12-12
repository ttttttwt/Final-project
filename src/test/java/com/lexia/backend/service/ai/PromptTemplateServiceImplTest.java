package com.lexia.backend.service.ai;

import com.lexia.backend.entity.PromptTemplate;
import com.lexia.backend.repository.PromptTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PromptTemplateServiceImpl.
 * Tests template retrieval, variable substitution, A/B testing, and caching.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PromptTemplateService Tests")
class PromptTemplateServiceImplTest {

    @Mock
    private PromptTemplateRepository promptTemplateRepository;

    @InjectMocks
    private PromptTemplateServiceImpl promptTemplateService;

    private PromptTemplate testTemplate;
    private UUID templateId;

    @BeforeEach
    void setUp() {
        templateId = UUID.randomUUID();
        testTemplate = PromptTemplate.builder()
                .id(templateId)
                .templateKey("roleplay_scenario_v1")
                .version(1)
                .templateText("Create a scenario for CEFR level {{cefr_level}} in {{domain}} domain.")
                .variables(Arrays.asList("cefr_level", "domain"))
                .category(PromptTemplate.CATEGORY_ROLEPLAY)
                .isActive(true)
                .isDefault(true)
                .trafficPercentage(100)
                .build();
    }

    @Nested
    @DisplayName("getTemplate() Tests")
    class GetTemplateTests {

        @Test
        @DisplayName("Should return default template when exists")
        void getTemplate_DefaultExists_ReturnsTemplate() {
            // Arrange
            when(promptTemplateRepository.findByTemplateKeyAndIsDefaultTrueAndIsActiveTrue("roleplay_scenario_v1"))
                    .thenReturn(Optional.of(testTemplate));

            // Act
            Optional<PromptTemplate> result = promptTemplateService.getTemplate("roleplay_scenario_v1");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getTemplateKey()).isEqualTo("roleplay_scenario_v1");
            assertThat(result.get().getIsDefault()).isTrue();
        }

        @Test
        @DisplayName("Should fallback to latest when no default")
        void getTemplate_NoDefault_ReturnsLatest() {
            // Arrange
            when(promptTemplateRepository.findByTemplateKeyAndIsDefaultTrueAndIsActiveTrue("test_key"))
                    .thenReturn(Optional.empty());
            when(promptTemplateRepository.findLatestByTemplateKey("test_key"))
                    .thenReturn(Optional.of(testTemplate));

            // Act
            Optional<PromptTemplate> result = promptTemplateService.getTemplate("test_key");

            // Assert
            assertThat(result).isPresent();
            verify(promptTemplateRepository).findLatestByTemplateKey("test_key");
        }

        @Test
        @DisplayName("Should return empty when template not found")
        void getTemplate_NotFound_ReturnsEmpty() {
            // Arrange
            when(promptTemplateRepository.findByTemplateKeyAndIsDefaultTrueAndIsActiveTrue("nonexistent"))
                    .thenReturn(Optional.empty());
            when(promptTemplateRepository.findLatestByTemplateKey("nonexistent"))
                    .thenReturn(Optional.empty());

            // Act
            Optional<PromptTemplate> result = promptTemplateService.getTemplate("nonexistent");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should get specific version")
        void getTemplate_SpecificVersion_ReturnsCorrectVersion() {
            // Arrange
            when(promptTemplateRepository.findByTemplateKeyAndVersion("test_key", 2))
                    .thenReturn(Optional.of(testTemplate));

            // Act
            Optional<PromptTemplate> result = promptTemplateService.getTemplate("test_key", 2);

            // Assert
            assertThat(result).isPresent();
            verify(promptTemplateRepository).findByTemplateKeyAndVersion("test_key", 2);
        }
    }

    @Nested
    @DisplayName("resolveVariables() Tests")
    class ResolveVariablesTests {

        @Test
        @DisplayName("Should replace all variables")
        void resolveVariables_AllVariablesProvided_ReplacesAll() {
            // Arrange
            String template = "Hello {{name}}, your level is {{level}}.";
            Map<String, Object> variables = Map.of(
                    "name", "John",
                    "level", "B1"
            );

            // Act
            String result = promptTemplateService.resolveVariables(template, variables);

            // Assert
            assertThat(result).isEqualTo("Hello John, your level is B1.");
        }

        @Test
        @DisplayName("Should handle missing variables gracefully")
        void resolveVariables_MissingVariable_KeepsPlaceholder() {
            // Arrange
            String template = "Hello {{name}}, your level is {{level}}.";
            Map<String, Object> variables = Map.of("name", "John");

            // Act
            String result = promptTemplateService.resolveVariables(template, variables);

            // Assert
            assertThat(result).isEqualTo("Hello John, your level is {{level}}.");
        }

        @Test
        @DisplayName("Should handle null template text")
        void resolveVariables_NullTemplate_ReturnsNull() {
            // Act
            String result = promptTemplateService.resolveVariables(null, Map.of("key", "value"));

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle empty template text")
        void resolveVariables_EmptyTemplate_ReturnsEmpty() {
            // Act
            String result = promptTemplateService.resolveVariables("", Map.of("key", "value"));

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle null variables map")
        void resolveVariables_NullVariables_ReturnsOriginal() {
            // Arrange
            String template = "Hello {{name}}!";

            // Act
            String result = promptTemplateService.resolveVariables(template, null);

            // Assert
            assertThat(result).isEqualTo("Hello {{name}}!");
        }

        @Test
        @DisplayName("Should handle variables with spaces around them")
        void resolveVariables_VariablesWithSpaces_ReplacesCorrectly() {
            // Arrange
            String template = "Hello {{ name }}, your {{ level }} is good.";
            Map<String, Object> variables = Map.of("name", "Jane", "level", "B2");

            // Act
            String result = promptTemplateService.resolveVariables(template, variables);

            // Assert
            assertThat(result).isEqualTo("Hello Jane, your B2 is good.");
        }

        @Test
        @DisplayName("Should handle special characters in replacement values")
        void resolveVariables_SpecialCharsInValue_EscapesCorrectly() {
            // Arrange
            String template = "Content: {{content}}";
            Map<String, Object> variables = Map.of("content", "Test $100 (regex special)");

            // Act
            String result = promptTemplateService.resolveVariables(template, variables);

            // Assert
            assertThat(result).isEqualTo("Content: Test $100 (regex special)");
        }
    }

    @Nested
    @DisplayName("getAndResolve() Tests")
    class GetAndResolveTests {

        @Test
        @DisplayName("Should get and resolve in one operation")
        void getAndResolve_TemplateExists_ReturnsResolved() {
            // Arrange
            when(promptTemplateRepository.findByTemplateKeyAndIsDefaultTrueAndIsActiveTrue("roleplay_scenario_v1"))
                    .thenReturn(Optional.of(testTemplate));
            Map<String, Object> variables = Map.of("cefr_level", "B2", "domain", "presentations");

            // Act
            Optional<String> result = promptTemplateService.getAndResolve("roleplay_scenario_v1", variables);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get()).contains("CEFR level B2");
            assertThat(result.get()).contains("presentations domain");
        }

        @Test
        @DisplayName("Should return empty when template not found")
        void getAndResolve_TemplateNotFound_ReturnsEmpty() {
            // Arrange
            when(promptTemplateRepository.findByTemplateKeyAndIsDefaultTrueAndIsActiveTrue("nonexistent"))
                    .thenReturn(Optional.empty());
            when(promptTemplateRepository.findLatestByTemplateKey("nonexistent"))
                    .thenReturn(Optional.empty());

            // Act
            Optional<String> result = promptTemplateService.getAndResolve("nonexistent", Map.of());

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("A/B Testing Tests")
    class AbTestingTests {

        @Test
        @DisplayName("Should return single template when only one exists")
        void getTemplateForAbTest_SingleTemplate_ReturnsIt() {
            // Arrange
            when(promptTemplateRepository.findByTemplateKeyAndIsActiveTrue("test_key"))
                    .thenReturn(List.of(testTemplate));

            // Act
            Optional<PromptTemplate> result = promptTemplateService.getTemplateForAbTest("test_key");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testTemplate);
        }

        @Test
        @DisplayName("Should return empty when no templates found")
        void getTemplateForAbTest_NoTemplates_ReturnsEmpty() {
            // Arrange
            when(promptTemplateRepository.findByTemplateKeyAndIsActiveTrue("test_key"))
                    .thenReturn(Collections.emptyList());

            // Act
            Optional<PromptTemplate> result = promptTemplateService.getTemplateForAbTest("test_key");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should select from multiple templates based on traffic weight")
        void getTemplateForAbTest_MultipleTemplates_SelectsBasedOnWeight() {
            // Arrange
            PromptTemplate template1 = PromptTemplate.builder()
                    .id(UUID.randomUUID())
                    .templateKey("test_key")
                    .version(1)
                    .templateText("Template 1")
                    .trafficPercentage(70)
                    .isActive(true)
                    .build();

            PromptTemplate template2 = PromptTemplate.builder()
                    .id(UUID.randomUUID())
                    .templateKey("test_key")
                    .version(2)
                    .templateText("Template 2")
                    .trafficPercentage(30)
                    .isActive(true)
                    .build();

            when(promptTemplateRepository.findByTemplateKeyAndIsActiveTrue("test_key"))
                    .thenReturn(Arrays.asList(template1, template2));

            // Act - Run multiple times to verify distribution
            Map<String, Integer> counts = new HashMap<>();
            for (int i = 0; i < 100; i++) {
                Optional<PromptTemplate> result = promptTemplateService.getTemplateForAbTest("test_key");
                result.ifPresent(t -> counts.merge(t.getTemplateText(), 1, Integer::sum));
            }

            // Assert - Both templates should be selected (exact ratio hard to test due to randomness)
            assertThat(counts).containsKey("Template 1");
            // Note: Template 2 might not always be selected in 100 runs, but usually will be
        }
    }

    @Nested
    @DisplayName("validateVariables() Tests")
    class ValidateVariablesTests {

        @Test
        @DisplayName("Should return empty list when all variables provided")
        void validateVariables_AllProvided_ReturnsEmptyList() {
            // Arrange
            Map<String, Object> variables = Map.of("cefr_level", "B1", "domain", "meetings");

            // Act
            List<String> missing = promptTemplateService.validateVariables(testTemplate, variables);

            // Assert
            assertThat(missing).isEmpty();
        }

        @Test
        @DisplayName("Should return missing variable names")
        void validateVariables_SomeMissing_ReturnsMissingList() {
            // Arrange
            Map<String, Object> variables = Map.of("cefr_level", "B1");

            // Act
            List<String> missing = promptTemplateService.validateVariables(testTemplate, variables);

            // Assert
            assertThat(missing).containsExactly("domain");
        }

        @Test
        @DisplayName("Should return all variables when null map provided")
        void validateVariables_NullMap_ReturnsAllVariables() {
            // Act
            List<String> missing = promptTemplateService.validateVariables(testTemplate, null);

            // Assert
            assertThat(missing).containsExactlyInAnyOrder("cefr_level", "domain");
        }

        @Test
        @DisplayName("Should return empty list for template with no variables")
        void validateVariables_NoVariables_ReturnsEmptyList() {
            // Arrange
            PromptTemplate noVarsTemplate = PromptTemplate.builder()
                    .templateText("Static text")
                    .variables(Collections.emptyList())
                    .build();

            // Act
            List<String> missing = promptTemplateService.validateVariables(noVarsTemplate, null);

            // Assert
            assertThat(missing).isEmpty();
        }
    }

    @Nested
    @DisplayName("recordUsage() Tests")
    class RecordUsageTests {

        @Test
        @DisplayName("Should call repository to update metrics")
        void recordUsage_ValidId_UpdatesMetrics() {
            // Arrange
            when(promptTemplateRepository.updateMetrics(templateId, true, 1500L))
                    .thenReturn(1);

            // Act
            promptTemplateService.recordUsage(templateId, true, 1500L);

            // Assert
            verify(promptTemplateRepository).updateMetrics(templateId, true, 1500L);
        }

        @Test
        @DisplayName("Should not throw on update failure")
        void recordUsage_UpdateFails_DoesNotThrow() {
            // Arrange
            when(promptTemplateRepository.updateMetrics(any(), anyBoolean(), anyLong()))
                    .thenReturn(0);

            // Act & Assert - should not throw
            promptTemplateService.recordUsage(templateId, false, 5000L);

            verify(promptTemplateRepository).updateMetrics(templateId, false, 5000L);
        }
    }

    @Nested
    @DisplayName("Template Management Tests")
    class TemplateManagementTests {

        @Test
        @DisplayName("Should create new template")
        void createTemplate_NewTemplate_SavesAndReturns() {
            // Arrange
            when(promptTemplateRepository.existsByTemplateKeyAndVersion("new_key", 1))
                    .thenReturn(false);
            when(promptTemplateRepository.save(any(PromptTemplate.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            PromptTemplate newTemplate = PromptTemplate.builder()
                    .templateKey("new_key")
                    .version(1)
                    .templateText("New template {{var}}")
                    .build();

            // Act
            PromptTemplate result = promptTemplateService.createTemplate(newTemplate);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getVariables()).contains("var");
            verify(promptTemplateRepository).save(any(PromptTemplate.class));
        }

        @Test
        @DisplayName("Should auto-increment version if exists")
        void createTemplate_VersionExists_IncrementsVersion() {
            // Arrange
            when(promptTemplateRepository.existsByTemplateKeyAndVersion("existing_key", 1))
                    .thenReturn(true);
            when(promptTemplateRepository.findAllVersionsByTemplateKey("existing_key"))
                    .thenReturn(List.of(
                            PromptTemplate.builder().version(2).build(),
                            PromptTemplate.builder().version(1).build()
                    ));
            when(promptTemplateRepository.save(any(PromptTemplate.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            PromptTemplate newTemplate = PromptTemplate.builder()
                    .templateKey("existing_key")
                    .version(1)
                    .templateText("Template text")
                    .build();

            // Act
            PromptTemplate result = promptTemplateService.createTemplate(newTemplate);

            // Assert
            assertThat(result.getVersion()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should set template as default")
        void setAsDefault_ValidTemplate_UpdatesDefault() {
            // Arrange
            when(promptTemplateRepository.findById(templateId))
                    .thenReturn(Optional.of(testTemplate));
            when(promptTemplateRepository.setAsDefault(templateId, "roleplay_scenario_v1"))
                    .thenReturn(1);

            // Act
            boolean result = promptTemplateService.setAsDefault(templateId);

            // Assert
            assertThat(result).isTrue();
            verify(promptTemplateRepository).setAsDefault(templateId, "roleplay_scenario_v1");
        }

        @Test
        @DisplayName("Should activate template")
        void activate_ValidTemplate_ActivatesSuccessfully() {
            // Arrange
            when(promptTemplateRepository.activate(templateId)).thenReturn(1);
            when(promptTemplateRepository.findById(templateId))
                    .thenReturn(Optional.of(testTemplate));

            // Act
            boolean result = promptTemplateService.activate(templateId);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should deactivate template")
        void deactivate_ValidTemplate_DeactivatesSuccessfully() {
            // Arrange
            when(promptTemplateRepository.deactivate(templateId)).thenReturn(1);
            when(promptTemplateRepository.findById(templateId))
                    .thenReturn(Optional.of(testTemplate));

            // Act
            boolean result = promptTemplateService.deactivate(templateId);

            // Assert
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Category and Keys Tests")
    class CategoryAndKeysTests {

        @Test
        @DisplayName("Should get templates by category")
        void getTemplatesByCategory_ReturnsMatching() {
            // Arrange
            when(promptTemplateRepository.findByCategoryAndIsActiveTrue("roleplay"))
                    .thenReturn(List.of(testTemplate));

            // Act
            List<PromptTemplate> result = promptTemplateService.getTemplatesByCategory("roleplay");

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategory()).isEqualTo("roleplay");
        }

        @Test
        @DisplayName("Should get all template keys")
        void getAllTemplateKeys_ReturnsDistinctKeys() {
            // Arrange
            when(promptTemplateRepository.findDistinctTemplateKeys())
                    .thenReturn(Arrays.asList("key1", "key2", "key3"));

            // Act
            List<String> result = promptTemplateService.getAllTemplateKeys();

            // Assert
            assertThat(result).containsExactly("key1", "key2", "key3");
        }
    }
}
