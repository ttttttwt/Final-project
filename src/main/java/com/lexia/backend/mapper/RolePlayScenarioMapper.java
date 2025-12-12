package com.lexia.backend.mapper;

import com.lexia.backend.dto.ai.RolePlayScenarioDTO;
import com.lexia.backend.dto.ai.RolePlayVocabularyItemDTO;
import com.lexia.backend.entity.RolePlayScenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapper utility class for converting between RolePlayScenario entities and DTOs.
 */
public class RolePlayScenarioMapper {

    private RolePlayScenarioMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static RolePlayScenarioDTO toDTO(RolePlayScenario scenario) {
        if (scenario == null) {
            return null;
        }

        return RolePlayScenarioDTO.builder()
                .id(scenario.getId())
                .title(scenario.getTitle())
                .context(scenario.getContext())
                .yourRole(scenario.getYourRole())
                .aiRole(scenario.getAiRole())
                .cefrLevel(scenario.getCefrLevel())
                .domain(scenario.getDomain())
                .industry(scenario.getIndustry())
                .objectives(safeList(scenario.getObjectives()))
                .keyVocabulary(mapVocabulary(scenario.getKeyVocabulary()))
                .openingLine(scenario.getOpeningLine())
                .suggestedDuration(scenario.getSuggestedDuration())
                .isFallback(scenario.getIsFallback())
                .createdAt(scenario.getCreatedAt())
                .build();
    }

    public static RolePlayScenario toEntity(RolePlayScenarioDTO dto) {
        if (dto == null) {
            return null;
        }

        return RolePlayScenario.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .context(dto.getContext())
                .yourRole(dto.getYourRole())
                .aiRole(dto.getAiRole())
                .cefrLevel(dto.getCefrLevel())
                .domain(dto.getDomain())
                .industry(dto.getIndustry())
                .objectives(safeList(dto.getObjectives()))
                .keyVocabulary(unmapVocabulary(dto.getKeyVocabulary()))
                .openingLine(dto.getOpeningLine())
                .suggestedDuration(dto.getSuggestedDuration())
                .isFallback(dto.getIsFallback())
                .createdAt(dto.getCreatedAt())
                .build();
    }

    private static List<RolePlayVocabularyItemDTO> mapVocabulary(List<Map<String, Object>> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }

        List<RolePlayVocabularyItemDTO> result = new ArrayList<>(raw.size());
        for (Map<String, Object> item : raw) {
            if (item == null) {
                continue;
            }
            result.add(RolePlayVocabularyItemDTO.builder()
                    .term(asString(item.get("term")))
                    .definition(asString(item.get("definition")))
                    .example(asString(item.get("example")))
                    .build());
        }
        return result;
    }

    private static List<Map<String, Object>> unmapVocabulary(List<RolePlayVocabularyItemDTO> raw) {
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> result = new ArrayList<>(raw.size());
        for (RolePlayVocabularyItemDTO item : raw) {
            if (item == null) {
                continue;
            }
            result.add(RolePlayScenario.vocabItem(item.getTerm(), item.getDefinition(), item.getExample()));
        }
        return result;
    }

    private static List<String> safeList(List<String> list) {
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
