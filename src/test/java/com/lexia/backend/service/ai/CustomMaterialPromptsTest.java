package com.lexia.backend.service.ai;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomMaterialPromptsTest {

    @Test
    void resolve_Success() {
        String template = "Hello {{name}}, welcome to {{place}}!";
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "John");
        variables.put("place", "Lexia");

        String result = CustomMaterialPrompts.resolve(template, variables);

        assertEquals("Hello John, welcome to Lexia!", result);
    }

    @Test
    void resolve_MissingVariable_ShouldLeavePlaceholder() {
        String template = "Hello {{name}}!";
        Map<String, Object> variables = new HashMap<>();
        // name is missing

        String result = CustomMaterialPrompts.resolve(template, variables);

        assertEquals("Hello {{name}}!", result);
    }

    @Test
    void resolve_NullVariables_ShouldReturnOriginal() {
        String template = "Hello {{name}}!";

        String result = CustomMaterialPrompts.resolve(template, null);

        assertEquals("Hello {{name}}!", result);
    }

    @Test
    void resolve_NullTemplate_ShouldReturnNull() {
        String result = CustomMaterialPrompts.resolve(null, new HashMap<>());
        assertEquals(null, result);
    }

    @Test
    void verifyConstantTemplates() {
        // Verify that templates are not null and contain expected keys
        assertTrue(CustomMaterialPrompts.VOCABULARY_PROMPT.contains("{{content}}"));
        assertTrue(CustomMaterialPrompts.QUIZ_PROMPT.contains("{{content}}"));
        assertTrue(CustomMaterialPrompts.SUMMARY_PROMPT.contains("{{content}}"));
        assertTrue(CustomMaterialPrompts.ROLEPLAY_PROMPT.contains("{{content}}"));
        assertTrue(CustomMaterialPrompts.ROLEPLAY_PROMPT.contains("{{cefr_level}}"));
        assertTrue(CustomMaterialPrompts.SHADOWING_PROMPT.contains("{{content}}"));
        assertTrue(CustomMaterialPrompts.COMBINED_PROMPT.contains("{{content}}"));
        assertTrue(CustomMaterialPrompts.STYLE_TRANSFORM_PROMPT.contains("{{content}}"));
        assertTrue(CustomMaterialPrompts.STYLE_TRANSFORM_PROMPT.contains("{{target_style}}"));
    }
}
