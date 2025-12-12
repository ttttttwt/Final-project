-- V26__Create_grammar_flashcard_tables.sql
-- Sprint 5 - Tasks C1 & D1: Grammar and Flashcard Tables
-- Create tables for AI-powered grammar exercises and flashcard features
-- Author: LEXIA Team
-- Date: December 12, 2025

-- ============================================================================
-- Grammar Topics Reference Table
-- Stores available grammar topics for exercise generation
-- ============================================================================
CREATE TABLE grammar_topics (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    cefr_levels VARCHAR(2)[] NOT NULL,
    description TEXT,
    examples TEXT[],
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Index for grammar topic queries
CREATE INDEX idx_grammar_topics_category ON grammar_topics(category);
CREATE INDEX idx_grammar_topics_active ON grammar_topics(is_active) WHERE is_active = true;

-- Comments for documentation
COMMENT ON TABLE grammar_topics IS 'Reference table of available grammar topics for AI exercise generation';
COMMENT ON COLUMN grammar_topics.cefr_levels IS 'Array of applicable CEFR levels: {A1,A2,B1,B2,C1,C2}';
COMMENT ON COLUMN grammar_topics.category IS 'Category: Tenses, Modals, Conditionals, Articles, Prepositions, etc.';

-- ============================================================================
-- Grammar Exercise Sets Table
-- Stores AI-generated and fallback grammar exercises
-- ============================================================================
CREATE TABLE grammar_exercise_sets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    topic_id INTEGER REFERENCES grammar_topics(id) ON DELETE SET NULL,
    grammar_point VARCHAR(100) NOT NULL,
    cefr_level VARCHAR(2) NOT NULL CHECK (cefr_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    theme VARCHAR(50),
    content JSONB NOT NULL,
    exercise_count INTEGER DEFAULT 0,
    time_limit_seconds INTEGER DEFAULT 600,
    is_fallback BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes for grammar exercise queries
CREATE INDEX idx_grammar_exercise_sets_level ON grammar_exercise_sets(cefr_level);
CREATE INDEX idx_grammar_exercise_sets_point ON grammar_exercise_sets(grammar_point);
CREATE INDEX idx_grammar_exercise_sets_user ON grammar_exercise_sets(user_id);
CREATE INDEX idx_grammar_exercise_sets_fallback ON grammar_exercise_sets(is_fallback) WHERE is_fallback = true;
CREATE INDEX idx_grammar_exercise_sets_level_point ON grammar_exercise_sets(cefr_level, grammar_point);
CREATE INDEX idx_grammar_exercise_sets_topic ON grammar_exercise_sets(topic_id);

-- Comments for documentation
COMMENT ON TABLE grammar_exercise_sets IS 'AI-generated grammar exercises with CEFR-level appropriate difficulty';
COMMENT ON COLUMN grammar_exercise_sets.grammar_point IS 'Grammar topic: Present Simple, Past Perfect, Conditionals, etc.';
COMMENT ON COLUMN grammar_exercise_sets.content IS 'JSON object: {explanation: {rule, examples, commonMistakes}, exercises: [{type, question, options?, correctAnswer, explanation, difficulty}]}';
COMMENT ON COLUMN grammar_exercise_sets.theme IS 'Contextual theme: workplace, travel, technology, etc.';
COMMENT ON COLUMN grammar_exercise_sets.is_fallback IS 'True for pre-seeded fallback content when AI is unavailable';
COMMENT ON COLUMN grammar_exercise_sets.time_limit_seconds IS 'Optional time limit for the exercise set (default 10 minutes)';

-- ============================================================================
-- User Grammar Progress Table
-- Tracks user progress and scores for grammar exercises
-- ============================================================================
CREATE TABLE user_grammar_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    exercise_set_id UUID NOT NULL REFERENCES grammar_exercise_sets(id) ON DELETE CASCADE,
    answers JSONB NOT NULL DEFAULT '[]',
    score INTEGER DEFAULT 0,
    max_score INTEGER DEFAULT 0,
    percentage DECIMAL(5,2) DEFAULT 0.00,
    time_spent_seconds INTEGER DEFAULT 0,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (user_id, exercise_set_id)
);

-- Indexes for grammar progress queries
CREATE INDEX idx_user_grammar_progress_user ON user_grammar_progress(user_id);
CREATE INDEX idx_user_grammar_progress_set ON user_grammar_progress(exercise_set_id);
CREATE INDEX idx_user_grammar_progress_completed ON user_grammar_progress(completed_at DESC);
CREATE INDEX idx_user_grammar_progress_user_completed ON user_grammar_progress(user_id, completed_at DESC);

-- Comments for documentation
COMMENT ON TABLE user_grammar_progress IS 'User submission records and scores for grammar exercises';
COMMENT ON COLUMN user_grammar_progress.answers IS 'JSON array: [{questionIndex, answer, correct, timeMs}]';
COMMENT ON COLUMN user_grammar_progress.percentage IS 'Score as percentage (0.00-100.00)';

-- ============================================================================
-- Flashcard Decks Table
-- Stores user flashcard decks from lessons or custom creation
-- ============================================================================
CREATE TABLE flashcard_decks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    source_type VARCHAR(20) CHECK (source_type IN ('lesson', 'ai_generated', 'user_created')),
    source_id BIGINT,
    cefr_level VARCHAR(2) CHECK (cefr_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    cards JSONB NOT NULL DEFAULT '[]',
    card_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    -- Ensure source_id is set only for lesson source_type
    CONSTRAINT check_source_consistency CHECK (
        (source_type = 'lesson' AND source_id IS NOT NULL) OR
        (source_type IN ('ai_generated', 'user_created'))
    )
);

-- Indexes for flashcard deck queries
CREATE INDEX idx_flashcard_decks_user ON flashcard_decks(user_id);
CREATE INDEX idx_flashcard_decks_source ON flashcard_decks(source_type, source_id);
CREATE INDEX idx_flashcard_decks_level ON flashcard_decks(cefr_level);
CREATE INDEX idx_flashcard_decks_user_source ON flashcard_decks(user_id, source_type);
CREATE INDEX idx_flashcard_decks_user_created ON flashcard_decks(user_id, created_at DESC);

-- Comments for documentation
COMMENT ON TABLE flashcard_decks IS 'User flashcard decks generated from lessons or created manually';
COMMENT ON COLUMN flashcard_decks.source_type IS 'lesson: from completed lesson, ai_generated: AI created, user_created: manual';
COMMENT ON COLUMN flashcard_decks.source_id IS 'Reference to lessons.id when source_type is lesson (BIGINT to match lessons.id type)';
COMMENT ON COLUMN flashcard_decks.cards IS 'JSON array: [{front, back: {definition, partOfSpeech, pronunciation, exampleSentence, synonyms, collocations}, tags, difficulty}]';
COMMENT ON COLUMN flashcard_decks.card_count IS 'Denormalized count of cards in the deck';

-- Trigger to update updated_at timestamp
CREATE TRIGGER update_flashcard_decks_updated_at
    BEFORE UPDATE ON flashcard_decks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- User Flashcard Progress Table
-- Tracks individual card mastery and spaced repetition scheduling (SM-2 algorithm)
-- ============================================================================
CREATE TABLE user_flashcard_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    deck_id UUID NOT NULL REFERENCES flashcard_decks(id) ON DELETE CASCADE,
    card_index INTEGER NOT NULL,
    mastery_level INTEGER DEFAULT 0 CHECK (mastery_level BETWEEN 0 AND 5),
    review_count INTEGER DEFAULT 0,
    correct_count INTEGER DEFAULT 0,
    ease_factor DECIMAL(4,2) DEFAULT 2.50 CHECK (ease_factor >= 1.30),
    interval_days INTEGER DEFAULT 1,
    last_reviewed_at TIMESTAMPTZ,
    next_review_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (user_id, deck_id, card_index)
);

-- Indexes for spaced repetition queries
CREATE INDEX idx_user_flashcard_progress_user_deck ON user_flashcard_progress(user_id, deck_id);
CREATE INDEX idx_user_flashcard_progress_next_review ON user_flashcard_progress(next_review_at);
CREATE INDEX idx_user_flashcard_progress_due ON user_flashcard_progress(user_id, next_review_at) 
    WHERE next_review_at IS NOT NULL;
CREATE INDEX idx_user_flashcard_progress_mastery ON user_flashcard_progress(user_id, mastery_level);

-- Comments for documentation
COMMENT ON TABLE user_flashcard_progress IS 'Per-card progress tracking with SM-2 spaced repetition algorithm support';
COMMENT ON COLUMN user_flashcard_progress.mastery_level IS '0: New, 1: Learning, 2: Young, 3: Mature, 4: Master, 5: Expert';
COMMENT ON COLUMN user_flashcard_progress.ease_factor IS 'SM-2 ease factor (1.30-2.50), affects interval calculation';
COMMENT ON COLUMN user_flashcard_progress.interval_days IS 'Days until next review (SM-2 algorithm)';
COMMENT ON COLUMN user_flashcard_progress.next_review_at IS 'When the card is due for review (for spaced repetition)';

-- ============================================================================
-- Insert Default Grammar Topics
-- Comprehensive list of grammar topics organized by category and CEFR level
-- ============================================================================
INSERT INTO grammar_topics (name, category, cefr_levels, description, examples) VALUES
-- Tenses
('Present Simple', 'Tenses', ARRAY['A1','A2'], 'Used for habits, routines, and general truths', 
 ARRAY['I work every day.', 'She speaks English fluently.']),
('Present Continuous', 'Tenses', ARRAY['A1','A2'], 'Used for actions happening now or temporary situations',
 ARRAY['I am working on a project.', 'They are learning English.']),
('Past Simple', 'Tenses', ARRAY['A2','B1'], 'Used for completed actions in the past',
 ARRAY['I worked late yesterday.', 'She finished the report.']),
('Past Continuous', 'Tenses', ARRAY['A2','B1'], 'Used for actions in progress at a specific past time',
 ARRAY['I was working when you called.', 'They were discussing the project.']),
('Present Perfect', 'Tenses', ARRAY['B1','B2'], 'Used for past actions with present relevance',
 ARRAY['I have completed the task.', 'She has worked here for 5 years.']),
('Present Perfect Continuous', 'Tenses', ARRAY['B1','B2'], 'Used for actions that started in the past and continue',
 ARRAY['I have been working on this all day.', 'She has been studying English.']),
('Past Perfect', 'Tenses', ARRAY['B2','C1'], 'Used for actions completed before another past action',
 ARRAY['I had finished before she arrived.', 'They had already left.']),
('Past Perfect Continuous', 'Tenses', ARRAY['B2','C1'], 'Used for duration of an action before a past event',
 ARRAY['I had been waiting for an hour when she called.', 'They had been working all day.']),
('Future Simple (will)', 'Tenses', ARRAY['A2','B1'], 'Used for predictions and spontaneous decisions',
 ARRAY['I will call you tomorrow.', 'It will rain later.']),
('Future with Going To', 'Tenses', ARRAY['A2','B1'], 'Used for planned intentions and predictions with evidence',
 ARRAY['I am going to start a new project.', 'Look at those clouds - it''s going to rain.']),
('Future Continuous', 'Tenses', ARRAY['B1','B2'], 'Used for actions in progress at a future time',
 ARRAY['I will be working at 3 PM.', 'This time tomorrow, I''ll be flying to London.']),
('Future Perfect', 'Tenses', ARRAY['B2','C1'], 'Used for actions completed before a future time',
 ARRAY['I will have finished by Friday.', 'She will have left by then.']),
('Future Perfect Continuous', 'Tenses', ARRAY['C1','C2'], 'Used for duration of an action up to a future time',
 ARRAY['By June, I will have been working here for 10 years.', 'She will have been studying for 5 hours by then.']),

-- Modals
('Modal Verbs - Ability (can/could)', 'Modals', ARRAY['A2','B1'], 'Can, could, be able to for expressing ability',
 ARRAY['I can speak English.', 'She could swim when she was 5.']),
('Modal Verbs - Permission (may/can/could)', 'Modals', ARRAY['A2','B1'], 'Asking for and giving permission',
 ARRAY['May I leave early?', 'You can use my computer.']),
('Modal Verbs - Obligation (must/have to)', 'Modals', ARRAY['B1','B2'], 'Must, have to, need to for obligation',
 ARRAY['You must finish this today.', 'I have to attend the meeting.']),
('Modal Verbs - Advice (should/ought to)', 'Modals', ARRAY['B1','B2'], 'Should, ought to for advice and recommendations',
 ARRAY['You should consider the options.', 'You ought to apologize.']),
('Modal Verbs - Possibility (may/might/could)', 'Modals', ARRAY['B1','B2'], 'May, might, could for expressing possibility',
 ARRAY['It might rain tomorrow.', 'She may be late.']),
('Modal Verbs - Deduction (must/can''t/might)', 'Modals', ARRAY['B2','C1'], 'Making logical deductions about situations',
 ARRAY['He must be tired after that journey.', 'She can''t be at home - her car isn''t there.']),
('Modal Perfects', 'Modals', ARRAY['B2','C1'], 'Modals with have + past participle for past situations',
 ARRAY['You should have told me.', 'She might have forgotten.']),

-- Conditionals
('Zero Conditional', 'Conditionals', ARRAY['A2','B1'], 'For general truths and scientific facts',
 ARRAY['If you heat water to 100°C, it boils.', 'If I miss breakfast, I feel tired.']),
('First Conditional', 'Conditionals', ARRAY['B1','B2'], 'For real/possible future situations',
 ARRAY['If it rains, I will take an umbrella.', 'If you study hard, you will pass.']),
('Second Conditional', 'Conditionals', ARRAY['B1','B2'], 'For hypothetical present/future situations',
 ARRAY['If I had more time, I would learn Spanish.', 'If she were here, she would help.']),
('Third Conditional', 'Conditionals', ARRAY['B2','C1'], 'For hypothetical past situations',
 ARRAY['If I had known, I would have helped.', 'If she had studied, she would have passed.']),
('Mixed Conditionals', 'Conditionals', ARRAY['C1','C2'], 'Mixing time references in conditionals',
 ARRAY['If I had studied harder, I would have a better job now.', 'If she were more careful, she wouldn''t have made that mistake.']),

-- Voice and Speech
('Passive Voice - Present/Past', 'Voice', ARRAY['B1','B2'], 'Passive constructions in present and past tenses',
 ARRAY['The report was written by John.', 'English is spoken worldwide.']),
('Passive Voice - Perfect Tenses', 'Voice', ARRAY['B2','C1'], 'Passive with perfect tenses',
 ARRAY['The project has been completed.', 'The decision had been made before I arrived.']),
('Passive Voice - Modals', 'Voice', ARRAY['B2','C1'], 'Passive with modal verbs',
 ARRAY['The work must be done by Friday.', 'Mistakes can be corrected.']),
('Reported Speech - Statements', 'Speech', ARRAY['B1','B2'], 'Reporting statements made by others',
 ARRAY['She said she was tired.', 'He told me he would come.']),
('Reported Speech - Questions', 'Speech', ARRAY['B1','B2'], 'Reporting questions',
 ARRAY['She asked if I had finished.', 'He wanted to know where I lived.']),
('Reported Speech - Commands', 'Speech', ARRAY['B2','C1'], 'Reporting commands and requests',
 ARRAY['She told me to wait.', 'He asked me not to be late.']),

-- Clauses
('Relative Clauses - Defining', 'Clauses', ARRAY['B1','B2'], 'Essential information about the noun',
 ARRAY['The man who called is my boss.', 'The book that I read was interesting.']),
('Relative Clauses - Non-defining', 'Clauses', ARRAY['B2','C1'], 'Additional, non-essential information',
 ARRAY['My boss, who lives in London, is retiring.', 'Paris, which is the capital of France, is beautiful.']),
('Noun Clauses', 'Clauses', ARRAY['B2','C1'], 'Clauses functioning as nouns',
 ARRAY['What he said surprised me.', 'I don''t know whether she''s coming.']),
('Adverbial Clauses', 'Clauses', ARRAY['B2','C1'], 'Clauses showing time, reason, condition, etc.',
 ARRAY['Although it was raining, we went out.', 'I''ll wait until you''re ready.']),

-- Other Grammar Points
('Articles (a/an/the)', 'Determiners', ARRAY['A2','B1','B2'], 'Using a, an, the correctly',
 ARRAY['I saw a dog. The dog was brown.', 'She is an engineer.']),
('Quantifiers', 'Determiners', ARRAY['A2','B1'], 'Some, any, much, many, few, little',
 ARRAY['I have some questions.', 'There isn''t much time left.']),
('Prepositions of Time', 'Prepositions', ARRAY['A2','B1'], 'At, on, in for time expressions',
 ARRAY['I wake up at 7 AM.', 'The meeting is on Monday.', 'She was born in 1990.']),
('Prepositions of Place', 'Prepositions', ARRAY['A2','B1'], 'At, on, in, between, among for location',
 ARRAY['I am at the office.', 'The book is on the table.', 'She lives in London.']),
('Prepositions of Movement', 'Prepositions', ARRAY['A2','B1'], 'To, into, through, across',
 ARRAY['I walked to the station.', 'She ran through the park.']),
('Gerunds and Infinitives', 'Verb Forms', ARRAY['B1','B2'], 'Using -ing forms and to + verb correctly',
 ARRAY['I enjoy working here.', 'I want to improve my English.']),
('Comparative Adjectives', 'Adjectives', ARRAY['A2','B1'], 'Comparing two things',
 ARRAY['This is better than that.', 'She is more experienced than him.']),
('Superlative Adjectives', 'Adjectives', ARRAY['A2','B1'], 'Expressing the highest degree',
 ARRAY['She is the most experienced.', 'This is the best solution.']),
('Question Tags', 'Questions', ARRAY['B1','B2'], 'Adding confirmation questions',
 ARRAY['You work here, don''t you?', 'She is coming, isn''t she?']),
('Phrasal Verbs', 'Verb Forms', ARRAY['B1','B2','C1'], 'Verbs with particles that change meaning',
 ARRAY['I need to look into this matter.', 'The meeting was called off.']),
('Inversion', 'Word Order', ARRAY['C1','C2'], 'Inverted word order for emphasis or in conditionals',
 ARRAY['Never have I seen such beauty.', 'Had I known, I would have helped.']),
('Cleft Sentences', 'Word Order', ARRAY['C1','C2'], 'Sentences starting with It is/was... that/who for emphasis',
 ARRAY['It was John who broke the window.', 'What I need is a holiday.']);

-- ============================================================================
-- Seed Fallback Grammar Exercise Sets
-- Pre-generated exercises for common grammar topics
-- ============================================================================
INSERT INTO grammar_exercise_sets (grammar_point, cefr_level, theme, content, exercise_count, is_fallback) VALUES
(
    'Present Simple',
    'A2',
    'workplace',
    '{
        "explanation": {
            "rule": "Use the Present Simple for habits, routines, and facts. Add -s/-es for he/she/it.",
            "examples": ["I work from 9 to 5.", "She sends emails every day.", "The office opens at 8 AM."],
            "commonMistakes": [
                {"mistake": "He work in the office.", "correction": "He works in the office.", "why": "Third person singular needs -s."}
            ]
        },
        "exercises": [
            {
                "id": 1,
                "type": "multiple_choice",
                "instruction": "Choose the correct form of the verb.",
                "question": "She ___ to the office every day.",
                "options": ["go", "goes", "going", "gone"],
                "correctAnswer": "goes",
                "explanation": "Third person singular (she) requires -es for verbs ending in -o.",
                "difficulty": 1
            },
            {
                "id": 2,
                "type": "fill_blank",
                "instruction": "Fill in the blank with the correct form of the verb in brackets.",
                "question": "My manager always ___ (check) the reports before meetings.",
                "correctAnswer": "checks",
                "explanation": "Third person singular requires -s.",
                "difficulty": 1
            },
            {
                "id": 3,
                "type": "error_correction",
                "instruction": "Find and correct the error.",
                "question": "The team have a meeting every Monday morning.",
                "correctAnswer": "The team has a meeting every Monday morning.",
                "explanation": "''Team'' is singular in British English workplace contexts, so it takes ''has''.",
                "difficulty": 2
            },
            {
                "id": 4,
                "type": "multiple_choice",
                "instruction": "Choose the correct form.",
                "question": "Our company ___ innovative software solutions.",
                "options": ["develop", "develops", "developing", "developed"],
                "correctAnswer": "develops",
                "explanation": "''Company'' is singular, so the verb needs -s.",
                "difficulty": 2
            },
            {
                "id": 5,
                "type": "transformation",
                "instruction": "Change to negative form.",
                "question": "I understand the instructions.",
                "correctAnswer": "I do not understand the instructions. / I don''t understand the instructions.",
                "explanation": "Use ''do not'' or ''don''t'' for negative Present Simple with I/you/we/they.",
                "difficulty": 2
            }
        ]
    }'::jsonb,
    5,
    true
),
(
    'First Conditional',
    'B1',
    'workplace',
    '{
        "explanation": {
            "rule": "Use the First Conditional for real/possible future situations. Structure: If + Present Simple, will + infinitive.",
            "examples": ["If we finish early, we will have a break.", "If the client agrees, we will start next week."],
            "commonMistakes": [
                {"mistake": "If it will rain, I will stay home.", "correction": "If it rains, I will stay home.", "why": "Use Present Simple, not ''will'', in the if-clause."}
            ]
        },
        "exercises": [
            {
                "id": 1,
                "type": "multiple_choice",
                "instruction": "Choose the correct form.",
                "question": "If the meeting ___ late, we will miss our train.",
                "options": ["will run", "runs", "ran", "running"],
                "correctAnswer": "runs",
                "explanation": "Use Present Simple in the if-clause of First Conditional.",
                "difficulty": 2
            },
            {
                "id": 2,
                "type": "fill_blank",
                "instruction": "Complete the sentence with the correct verb form.",
                "question": "If she ___ (complete) the project on time, she will get a bonus.",
                "correctAnswer": "completes",
                "explanation": "The if-clause uses Present Simple.",
                "difficulty": 2
            },
            {
                "id": 3,
                "type": "transformation",
                "instruction": "Combine into a First Conditional sentence.",
                "question": "You work hard. You will succeed.",
                "correctAnswer": "If you work hard, you will succeed.",
                "explanation": "The condition goes in the if-clause with Present Simple.",
                "difficulty": 3
            },
            {
                "id": 4,
                "type": "error_correction",
                "instruction": "Find and correct the error.",
                "question": "If the client will accept our proposal, we will begin immediately.",
                "correctAnswer": "If the client accepts our proposal, we will begin immediately.",
                "explanation": "Don''t use ''will'' in the if-clause; use Present Simple.",
                "difficulty": 3
            },
            {
                "id": 5,
                "type": "multiple_choice",
                "instruction": "Select the correct option.",
                "question": "___ you send me the report, I will review it tonight.",
                "options": ["If", "When", "Unless", "Both A and B"],
                "correctAnswer": "Both A and B",
                "explanation": "Both ''If'' and ''When'' can introduce real future conditions.",
                "difficulty": 3
            }
        ]
    }'::jsonb,
    5,
    true
);
