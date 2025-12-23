package com.lexia.backend.service.ai;

/**
 * Prompt templates for Custom Material AI content generation.
 * 
 * <p>
 * Uses XML tagging for security (prompt injection prevention)
 * as specified in CUSTOM-AI-CONTENT-GENERATOR-SPEC.md
 * </p>
 * 
 * @since Sprint 5
 */
public final class CustomMaterialPrompts {

  private CustomMaterialPrompts() {
  } // Utility class

  // ===== VOCABULARY EXTRACTION =====

  public static final String VOCABULARY_PROMPT = """
      You are an English language learning expert. Extract vocabulary from the provided content.

      <user_content>
      {{content}}
      </user_content>

      <instructions>
      Extract 10-15 important vocabulary words/phrases suitable for {{cefr_level}} level learners.
      Focus on: professional terms, idiomatic expressions, and contextually important words.

      For each word, provide:
      1. The word/phrase
      2. IPA pronunciation (International Phonetic Alphabet)
      3. Part of speech
      4. Definition in simple English
      5. Example sentence from the content
      6. Context about how it's used

      Return as JSON array with this exact structure:
      ```json
      [
        {
          "id": "v1",
          "word": "stakeholder",
          "ipa": "ˈsteɪkˌhoʊldər",
          "partOfSpeech": "noun",
          "definition": "A person with an interest or concern in something",
          "example": "All stakeholders must approve the budget.",
          "context": "Used in business meetings to refer to interested parties"
        }
      ]
      ```
      </instructions>

      Return ONLY valid JSON, no markdown formatting or explanations.
      """;

  // ===== QUIZ GENERATION =====

  public static final String QUIZ_PROMPT = """
      You are an English language learning expert. Create comprehension and vocabulary quiz questions.

      <user_content>
      {{content}}
      </user_content>

      <instructions>
      Create 5-8 quiz questions for {{cefr_level}} level learners based on this content.
      Mix question types: multiple choice (3-4 options), true/false, fill-in-blank.

      Focus on:
      - Reading comprehension (main ideas, details)
      - Vocabulary in context
      - Grammar patterns used in the text

      Return as JSON array with this exact structure:
      ```json
      [
        {
          "id": "q1",
          "type": "multiple_choice",
          "question": "What is the main purpose of the quarterly report?",
          "options": ["Option A", "Option B", "Option C", "Option D"],
          "correctAnswers": ["Option B"],
          "explanation": "The report clearly states..."
        },
        {
          "id": "q2",
          "type": "true_false",
          "question": "The company exceeded its Q1 targets.",
          "options": ["True", "False"],
          "correctAnswers": ["True"],
          "explanation": "As mentioned in paragraph 2..."
        },
        {
          "id": "q3",
          "type": "fill_blank",
          "question": "The marketing team achieved a ___ increase in engagement.",
          "options": ["significant", "marginal", "negative"],
          "correctAnswers": ["significant"],
          "explanation": "The word 'significant' appears in the context..."
        }
      ]
      ```
      </instructions>

      Return ONLY valid JSON, no markdown formatting or explanations.
      """;

  // ===== SUMMARY GENERATION =====

  public static final String SUMMARY_PROMPT = """
      You are an English language learning expert. Create a smart summary for language learning.

      <user_content>
      {{content}}
      </user_content>

      <instructions>
      Create a summary suitable for {{cefr_level}} level English learners.

      Requirements:
      1. Write 3-5 paragraphs summarizing the main points
      2. Use vocabulary appropriate for {{cefr_level}} level
      3. Highlight key terms in **bold**
      4. Keep sentences clear and not too complex

      The summary should help learners:
      - Understand the main ideas quickly
      - See key vocabulary in context
      - Prepare for discussing this topic

      Return the summary as plain text with markdown formatting.
      </instructions>
      """;

  // ===== ROLE-PLAY SCENARIO =====

  public static final String ROLEPLAY_PROMPT = """
      You are an English conversation design expert. Create a role-play scenario.

      <user_content>
      {{content}}
      </user_content>

      <instructions>
      Create a realistic role-play scenario based on this content for {{cefr_level}} level learners.

      Design a scenario where:
      1. User practices English relevant to the content topic.
      2. The scenario is an **ongoing conversation** or a **continuation of a story** (not starting from scratch). The AI should act as if the interaction is already in progress.
      3. The AI **must prioritize** using vocabulary, sentence patterns, and specific content from the `<user_content>`. If the content is a list of vocabulary, the AI should aim to use as many of those words as possible in the conversation.
      4. AI plays a role that encourages natural conversation and initiates the interaction.
      5. The scenario has clear goals and context.

      Return as JSON with this exact structure:
      ```json
      {
        "title": "Quarterly Business Review Meeting",
        "context": "You are in a weekly team meeting discussing the quarterly results.",
        "contextDetails": {
          "setting": "Modern tech company office during a team meeting",
          "situation": "The team is reviewing Q1 performance and planning for Q2. You have just finished your introduction and the CEO is asking for details.",
          "keyInfo": [
            "Project deadline is next Friday",
            "Budget for Q2 is increased by 10%",
            "User engagement grew by 15% in Q1"
          ],
          "yourGoal": "Present the marketing results and propose a new strategy for Q2",
          "tips": [
            "Start with a brief overview of the metrics",
            "Be prepared to explain the dip in social media engagement"
          ]
        },
        "yourRole": "Marketing Manager",
        "aiRole": "CEO (Sarah)",
        "objectives": [
          "Explain the key marketing metrics",
          "Discuss challenges faced",
          "Propose next quarter's strategy"
        ],
        "suggestedPrompts": [
          "I'd like to start with our digital marketing results.",
          "We faced some challenges with our target audience.",
          "For next quarter, I'm proposing a new approach."
        ],
        "keyVocabulary": [
          {
            "term": "ROI",
            "ipa": "/ˌɑːr.oʊˈaɪ/",
            "definition": "Return on Investment; a measure of the profit made from an investment",
            "example": "We need to improve the ROI of our ad campaigns."
          },
          {
            "term": "conversion rate",
            "ipa": "/kənˈvɜːr.ʒən reɪt/",
            "definition": "The percentage of users who take a desired action",
            "example": "Our landing page has a 5% conversion rate."
          }
        ],
        "openingLine": "Hi there! I'm looking forward to hearing your update on the Q1 marketing performance. Shall we begin?"
      }
      ```
      </instructions>

      Return ONLY valid JSON, no markdown formatting or explanations.
      """;

  // ===== SHADOWING CONTENT =====

  public static final String SHADOWING_PROMPT = """
      You are an English pronunciation and speaking coach. Create shadowing practice content.

      <user_content>
      {{content}}
      </user_content>

      <instructions>
      Extract 5-10 sentences from the content that are good for shadowing practice.

      Choose sentences that:
      1. Are natural spoken English (not too formal or written)
      2. Have interesting pronunciation features (stress, intonation)
      3. Contain useful phrases or expressions
      4. Are appropriate length (5-15 words)

      For each sentence, provide:
      1. The sentence
      2. A simplified phonetic guide (not IPA, use readable notation)
      3. Notes on stress and intonation patterns

      Return as JSON array with this exact structure:
      ```json
      [
        {
          "id": "s1",
          "sentence": "Let me walk you through the quarterly results.",
          "phonetic": "LET me WALK you through the QUARTER-ly re-SULTS",
          "notes": "Stress on 'let', 'walk', 'quarterly', 'results'. Falling intonation at end."
        }
      ]
      ```
      </instructions>

      Return ONLY valid JSON, no markdown formatting or explanations.
      """;

  // ===== COMBINED GENERATION =====

  public static final String COMBINED_PROMPT = """
      You are an English language learning content creator. Generate learning materials from the provided content.

      <user_content>
      {{content}}
      </user_content>

      <target_options>
      {{target_options}}
      </target_options>

      <instructions>
      Based on the target options, generate the requested learning materials for {{cefr_level}} level learners.

      If VOCABULARY is requested: Extract 10-15 vocabulary items with structure:
        {"id": "v1", "word": "term", "ipa": "IPA pronunciation", "partOfSpeech": "noun", "definition": "...", "example": "...", "context": "..."}
      If QUIZ is requested: Create 5-8 quiz questions
      If SUMMARY is requested: Write a 3-5 paragraph summary
      If ROLE_PLAY is requested: Create a role-play scenario with structure:
        {
          "title": "...",
          "context": "...",
          "contextDetails": {
            "setting": "...", 
            "situation": "... (Design this as an ongoing conversation or continuation of a story based on the content)", 
            "keyInfo": ["..."], 
            "yourGoal": "...", 
            "tips": ["..."]
          },
          "yourRole": "...",
          "aiRole": "...",
          "objectives": ["..."],
          "suggestedPrompts": ["..."],
          "keyVocabulary": [{"term": "...", "ipa": "...", "definition": "...", "example": "..."}],
          "openingLine": "... (The AI's first message to the user, initiating the interaction. AI must prioritize using vocabulary and patterns from the content)"
        }
      If SHADOWING is requested: Extract 5-10 shadowing sentences

      Return as JSON with this structure (include only requested sections):
      ```json
      {
        "schemaVersion": 1,
        "vocabulary": [{"id": "v1", "word": "...", "ipa": "...", "partOfSpeech": "...", "definition": "...", "example": "...", "context": "..."}],
        "quiz": [...],
        "summary": "...",
        "roleplay": {...},
        "shadowing": [...]
      }
      ```
      </instructions>

      Return ONLY valid JSON, no markdown formatting or explanations.
      """;

  // ===== STYLE TRANSFORM =====

  public static final String STYLE_TRANSFORM_PROMPT = """
      You are an English language style expert. Transform the text into a different style.

      <user_content>
      {{content}}
      </user_content>

      <target_style>
      {{target_style}}
      </target_style>

      <instructions>
      Transform the content from its current style to {{target_style}} style.

      Target style options:
      - formal: Professional, academic language
      - casual: Friendly, conversational tone
      - email: Business email format
      - presentation: Bullet points, clear structure
      - social_media: Short, engaging, hashtags
      - diplomatic: Polite, considerate, softens demands while maintaining clarity
      - persuasive: Convincing, uses rhetorical techniques, emphasizes benefits

      {{#learn_mode}}
      Also provide explanations of the key changes made and why.
      Format your response as:

      ## Transformed Text
      [The transformed text]

      ## Key Changes
      - [Change 1]: [Explanation]
      - [Change 2]: [Explanation]
      ...
      {{/learn_mode}}

      {{^learn_mode}}
      Return only the transformed text without explanations.
      {{/learn_mode}}
      </instructions>
      """;

  // ===== HELPER: Build prompt with variables =====

  /**
   * Replaces {{variable}} placeholders in a template.
   * 
   * @param template  The template with placeholders
   * @param variables Map of variable names to values
   * @return The resolved template
   */
  public static String resolve(String template, java.util.Map<String, Object> variables) {
    if (template == null) {
      return null;
    }
    if (variables == null) {
      return template;
    }
    String result = template;
    for (var entry : variables.entrySet()) {
      result = result.replace("{{" + entry.getKey() + "}}",
          entry.getValue() != null ? entry.getValue().toString() : "");
    }
    return result;
  }
}
