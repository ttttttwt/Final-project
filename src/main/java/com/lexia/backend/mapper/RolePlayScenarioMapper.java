package com.lexia.backend.mapper;

import com.lexia.backend.dto.ai.RolePlayContextDetailsDTO;
import com.lexia.backend.dto.ai.RolePlayScenarioDTO;
import com.lexia.backend.dto.ai.RolePlayVocabularyItemDTO;
import com.lexia.backend.entity.RolePlayScenario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper utility class for converting between RolePlayScenario entities and
 * DTOs.
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
                .contextDetails(mapContextDetails(scenario.getContextDetails()))
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
                .suggestedPrompts(safeList(scenario.getSuggestedPrompts()))
                .agenda(safeList(scenario.getAgenda()))
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
                .contextDetails(unmapContextDetails(dto.getContextDetails()))
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
                .suggestedPrompts(safeList(dto.getSuggestedPrompts()))
                .agenda(safeList(dto.getAgenda()))
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

    @SuppressWarnings("unchecked")
    private static RolePlayContextDetailsDTO mapContextDetails(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }

        return RolePlayContextDetailsDTO.builder()
                .setting(asString(raw.get("setting")))
                .situation(asString(raw.get("situation")))
                .keyInfo(raw.get("keyInfo") instanceof List ? (List<String>) raw.get("keyInfo") : null)
                .yourGoal(asString(raw.get("yourGoal")))
                .tips(raw.get("tips") instanceof List ? (List<String>) raw.get("tips") : null)
                .build();
    }

    private static Map<String, Object> unmapContextDetails(RolePlayContextDetailsDTO dto) {
        if (dto == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        if (dto.getSetting() != null)
            result.put("setting", dto.getSetting());
        if (dto.getSituation() != null)
            result.put("situation", dto.getSituation());
        if (dto.getKeyInfo() != null)
            result.put("keyInfo", dto.getKeyInfo());
        if (dto.getYourGoal() != null)
            result.put("yourGoal", dto.getYourGoal());
        if (dto.getTips() != null)
            result.put("tips", dto.getTips());
        return result.isEmpty() ? null : result;
    }
}
