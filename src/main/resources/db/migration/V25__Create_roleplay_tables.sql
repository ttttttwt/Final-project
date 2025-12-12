-- V25__Create_roleplay_tables.sql
-- Sprint 5 - Task B1: Role-Play Tables
-- Create tables for AI-powered role-play conversation feature
-- Author: LEXIA Team
-- Date: December 12, 2025

-- ============================================================================
-- Role-Play Scenarios Table
-- Stores pre-generated and AI-generated conversation scenarios
-- ============================================================================
CREATE TABLE roleplay_scenarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    context TEXT NOT NULL,
    your_role VARCHAR(100) NOT NULL,
    ai_role VARCHAR(100) NOT NULL,
    cefr_level VARCHAR(2) NOT NULL CHECK (cefr_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    domain VARCHAR(50) NOT NULL,
    industry VARCHAR(50),
    objectives JSONB NOT NULL DEFAULT '[]',
    key_vocabulary JSONB NOT NULL DEFAULT '[]',
    opening_line TEXT NOT NULL,
    suggested_duration INTEGER DEFAULT 10,
    is_fallback BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes for efficient scenario lookup
CREATE INDEX idx_roleplay_scenarios_level_domain ON roleplay_scenarios(cefr_level, domain);
CREATE INDEX idx_roleplay_scenarios_fallback ON roleplay_scenarios(is_fallback) WHERE is_fallback = true;
CREATE INDEX idx_roleplay_scenarios_industry ON roleplay_scenarios(industry) WHERE industry IS NOT NULL;

-- Comments for documentation
COMMENT ON TABLE roleplay_scenarios IS 'AI role-play conversation scenarios with CEFR-level appropriate content';
COMMENT ON COLUMN roleplay_scenarios.cefr_level IS 'Common European Framework: A1 (Beginner) to C2 (Proficient)';
COMMENT ON COLUMN roleplay_scenarios.domain IS 'Business English domain: meetings, negotiations, presentations, emails, customer_service, interviews';
COMMENT ON COLUMN roleplay_scenarios.objectives IS 'JSON array of learning objectives for the scenario';
COMMENT ON COLUMN roleplay_scenarios.key_vocabulary IS 'JSON array of key vocabulary items: [{term, definition, example}]';
COMMENT ON COLUMN roleplay_scenarios.is_fallback IS 'True for pre-seeded fallback content when AI is unavailable';
COMMENT ON COLUMN roleplay_scenarios.suggested_duration IS 'Suggested conversation duration in minutes';

-- ============================================================================
-- Role-Play Conversations Table
-- Stores user conversation sessions with message history
-- ============================================================================
CREATE TABLE roleplay_conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    scenario_id UUID REFERENCES roleplay_scenarios(id) ON DELETE SET NULL,
    messages JSONB NOT NULL DEFAULT '[]',
    context_summary TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'in_progress' CHECK (status IN ('in_progress', 'completed', 'abandoned')),
    mode VARCHAR(20) NOT NULL DEFAULT 'immersive' CHECK (mode IN ('immersive', 'learning')),
    metrics JSONB DEFAULT '{"messageCount": 0, "userWordCount": 0, "aiWordCount": 0, "grammarScore": null, "vocabularyScore": null}',
    feedback_summary JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes for conversation queries
CREATE INDEX idx_roleplay_conversations_user ON roleplay_conversations(user_id);
CREATE INDEX idx_roleplay_conversations_status ON roleplay_conversations(status);
CREATE INDEX idx_roleplay_conversations_user_status ON roleplay_conversations(user_id, status);
CREATE INDEX idx_roleplay_conversations_created ON roleplay_conversations(created_at DESC);
CREATE INDEX idx_roleplay_conversations_scenario ON roleplay_conversations(scenario_id) WHERE scenario_id IS NOT NULL;

-- Comments for documentation
COMMENT ON TABLE roleplay_conversations IS 'User conversation sessions for AI role-play feature';
COMMENT ON COLUMN roleplay_conversations.messages IS 'JSON array of messages: [{role: "user"|"ai", content, timestamp, feedback?, vocabularyUsed?}]';
COMMENT ON COLUMN roleplay_conversations.context_summary IS 'AI-generated summary of older messages for context window management';
COMMENT ON COLUMN roleplay_conversations.mode IS 'immersive: fast chat only (<1s), learning: chat with feedback (<3s)';
COMMENT ON COLUMN roleplay_conversations.metrics IS 'Conversation statistics: message count, word counts, grammar/vocabulary scores';
COMMENT ON COLUMN roleplay_conversations.feedback_summary IS 'Aggregated feedback at conversation end: grammar issues, vocabulary suggestions, overall assessment';

-- Trigger to update updated_at timestamp
CREATE TRIGGER update_roleplay_conversations_updated_at
    BEFORE UPDATE ON roleplay_conversations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- Seed Fallback Role-Play Scenarios
-- Pre-generated scenarios for each CEFR level and domain combination
-- ============================================================================

-- A2 Level - Basic Workplace Scenarios
INSERT INTO roleplay_scenarios (title, context, your_role, ai_role, cefr_level, domain, industry, objectives, key_vocabulary, opening_line, suggested_duration, is_fallback) VALUES
(
    'First Day at Work',
    'You are starting a new job at a technology company. You need to introduce yourself to your new colleague and learn about the office.',
    'New Employee',
    'Experienced Colleague (Alex)',
    'A2',
    'workplace',
    'technology',
    '["Introduce yourself professionally", "Ask simple questions about the workplace", "Understand basic office vocabulary"]'::jsonb,
    '[{"term": "colleague", "definition": "a person you work with", "example": "My colleagues are very friendly."}, {"term": "department", "definition": "a section of a company", "example": "I work in the IT department."}, {"term": "meeting room", "definition": "a room for meetings", "example": "The meeting room is on the second floor."}]'::jsonb,
    'Hello! You must be the new team member. Welcome to TechCorp! I''m Alex. How are you feeling on your first day?',
    5,
    true
),
(
    'Ordering Lunch',
    'You are at the company cafeteria during lunch break. You want to order food and find a place to sit.',
    'Employee',
    'Cafeteria Staff',
    'A2',
    'workplace',
    NULL,
    '["Order food politely", "Ask about menu items", "Use polite expressions"]'::jsonb,
    '[{"term": "menu", "definition": "a list of food you can order", "example": "Can I see the menu, please?"}, {"term": "vegetarian", "definition": "food without meat", "example": "Do you have vegetarian options?"}, {"term": "to-go", "definition": "food to take away", "example": "I''d like my order to-go, please."}]'::jsonb,
    'Good afternoon! Welcome to the cafeteria. What can I get for you today?',
    5,
    true
),

-- B1 Level - Professional Scenarios
(
    'Project Status Meeting',
    'You are in a weekly team meeting. Your manager wants to know the status of your current project.',
    'Team Member',
    'Project Manager (Sarah)',
    'B1',
    'meetings',
    'technology',
    '["Report project progress clearly", "Explain any challenges", "Discuss next steps"]'::jsonb,
    '[{"term": "deadline", "definition": "the date when something must be finished", "example": "The deadline is next Friday."}, {"term": "milestone", "definition": "an important point in a project", "example": "We reached our first milestone yesterday."}, {"term": "blocker", "definition": "something that stops progress", "example": "We have a blocker with the API integration."}]'::jsonb,
    'Good morning everyone! Let''s go around the table. Can you give us an update on your current tasks?',
    10,
    true
),
(
    'Client Phone Call',
    'A client is calling to ask about their order status. You need to help them and provide information.',
    'Customer Service Representative',
    'Client (Mr. Johnson)',
    'B1',
    'customer_service',
    NULL,
    '["Handle customer inquiries professionally", "Provide clear information", "Offer solutions to problems"]'::jsonb,
    '[{"term": "order number", "definition": "a unique code for your purchase", "example": "Can you give me your order number?"}, {"term": "estimated delivery", "definition": "when you expect to receive something", "example": "The estimated delivery is Thursday."}, {"term": "refund", "definition": "money returned to you", "example": "I can process a refund if you prefer."}]'::jsonb,
    'Hello, this is Michael Johnson. I placed an order last week and I haven''t received any shipping confirmation yet. Can you help me?',
    10,
    true
),

-- B2 Level - Advanced Professional Scenarios
(
    'Salary Negotiation',
    'You have received a job offer and are now discussing the salary and benefits package with the HR manager.',
    'Job Candidate',
    'HR Manager (Patricia)',
    'B2',
    'negotiations',
    NULL,
    '["Negotiate professionally and confidently", "Justify your salary expectations", "Discuss benefits and conditions"]'::jsonb,
    '[{"term": "compensation package", "definition": "total salary plus benefits", "example": "The compensation package includes health insurance."}, {"term": "market rate", "definition": "typical salary for similar jobs", "example": "The market rate for this position is higher."}, {"term": "flexible working", "definition": "ability to choose working hours or location", "example": "Do you offer flexible working arrangements?"}]'::jsonb,
    'Thank you for accepting our offer in principle. I understand you''d like to discuss the compensation package. What aspects would you like to address?',
    15,
    true
),
(
    'Presenting to Stakeholders',
    'You are presenting your quarterly results to company stakeholders. They will ask questions about performance and future plans.',
    'Department Head',
    'Stakeholder (Board Member)',
    'B2',
    'presentations',
    'finance',
    '["Present data clearly and confidently", "Handle questions professionally", "Explain business decisions"]'::jsonb,
    '[{"term": "revenue growth", "definition": "increase in money earned", "example": "We achieved 15% revenue growth this quarter."}, {"term": "ROI", "definition": "return on investment - profit compared to cost", "example": "The ROI on this project exceeded expectations."}, {"term": "forecast", "definition": "prediction about the future", "example": "Our forecast for next quarter is optimistic."}]'::jsonb,
    'Thank you for the presentation. I''d like to understand more about the Q3 figures. What drove the variance from the projected targets?',
    15,
    true
),

-- C1 Level - Complex Professional Scenarios
(
    'Crisis Management Discussion',
    'Your company is facing a PR crisis due to a product defect. You need to discuss the response strategy with senior leadership.',
    'Communications Director',
    'CEO (David)',
    'C1',
    'meetings',
    NULL,
    '["Discuss complex issues diplomatically", "Propose strategic solutions", "Consider multiple stakeholder perspectives"]'::jsonb,
    '[{"term": "damage control", "definition": "actions to limit negative effects", "example": "We need immediate damage control."}, {"term": "stakeholder communication", "definition": "informing people affected by the company", "example": "Our stakeholder communication must be transparent."}, {"term": "press release", "definition": "official statement to media", "example": "The press release should acknowledge the issue directly."}]'::jsonb,
    'This situation requires immediate attention. The board is concerned about our reputation. What''s your recommended communication strategy?',
    15,
    true
),
(
    'Merger Integration Planning',
    'Your company is acquiring a smaller competitor. You are discussing integration plans with the leadership team.',
    'Integration Lead',
    'CFO (Jennifer)',
    'C1',
    'negotiations',
    'finance',
    '["Navigate complex business discussions", "Balance competing priorities", "Communicate integration challenges"]'::jsonb,
    '[{"term": "due diligence", "definition": "careful examination before a business deal", "example": "Due diligence revealed some concerns."}, {"term": "synergies", "definition": "benefits from combining two companies", "example": "We expect significant cost synergies."}, {"term": "cultural integration", "definition": "combining different company cultures", "example": "Cultural integration will be our biggest challenge."}]'::jsonb,
    'The acquisition will close next month. We need to finalize the integration timeline. What are the critical dependencies we should address first?',
    15,
    true
);

-- Add more A1 scenarios for beginners
INSERT INTO roleplay_scenarios (title, context, your_role, ai_role, cefr_level, domain, industry, objectives, key_vocabulary, opening_line, suggested_duration, is_fallback) VALUES
(
    'Meeting a New Colleague',
    'You meet a new person in the office kitchen. You want to introduce yourself and make small talk.',
    'Employee',
    'New Colleague (Sam)',
    'A1',
    'workplace',
    NULL,
    '["Say hello and introduce yourself", "Ask simple questions", "Use basic greetings"]'::jsonb,
    '[{"term": "nice to meet you", "definition": "a polite greeting when meeting someone new", "example": "Nice to meet you, Sam."}, {"term": "job", "definition": "work that you do", "example": "What is your job?"}, {"term": "office", "definition": "place where you work", "example": "This is a nice office."}]'::jsonb,
    'Oh, hello! I don''t think we''ve met. I''m Sam, I just started here.',
    5,
    true
),
(
    'Asking for Help',
    'You need help with the printer in the office. You ask a colleague for assistance.',
    'Employee',
    'Helpful Colleague',
    'A1',
    'workplace',
    NULL,
    '["Ask for help politely", "Describe a simple problem", "Say thank you"]'::jsonb,
    '[{"term": "help", "definition": "to do something for someone", "example": "Can you help me?"}, {"term": "printer", "definition": "a machine that prints paper", "example": "The printer is not working."}, {"term": "please", "definition": "a polite word when asking", "example": "Please help me."}]'::jsonb,
    'Hi there! You look a bit confused. Is everything okay?',
    5,
    true
),
-- C2 Level - Expert Professional Scenarios
(
    'Strategic Partnership Negotiation',
    'You are negotiating a complex strategic partnership agreement worth $50M with a major tech company. Multiple stakeholders have competing interests, and both sides have significant leverage points.',
    'Chief Strategy Officer',
    'Partner CEO (Richard)',
    'C2',
    'negotiations',
    'technology',
    '["Navigate nuanced business discussions with sophisticated language", "Handle implicit meanings and subtext", "Employ advanced negotiation tactics diplomatically"]'::jsonb,
    '[{"term": "synergy", "definition": "combined effect greater than individual parts", "example": "The synergies from this partnership could transform both organizations."}, {"term": "due diligence", "definition": "thorough investigation before a business decision", "example": "Our due diligence has raised some concerns about scalability."}, {"term": "equity stake", "definition": "ownership percentage in a company", "example": "We''re proposing a 15% equity stake in exchange for exclusive distribution rights."}, {"term": "leverage", "definition": "strategic advantage in negotiations", "example": "Your market position gives you considerable leverage."}, {"term": "non-compete clause", "definition": "agreement not to work with competitors", "example": "The non-compete clause would need to be geographically limited."}]'::jsonb,
    'I appreciate you taking the time to meet in person. Before we delve into specifics, I wanted to candidly discuss what success looks like for both organizations. Our board has some reservations about the exclusivity terms, but I believe there''s a path forward if we can align on the strategic vision.',
    20,
    true
),
(
    'Crisis Board Presentation',
    'Your company faces a regulatory investigation that could result in significant fines. You must present the situation and proposed response to the board of directors while managing expectations and maintaining confidence.',
    'Chief Legal Officer',
    'Board Chair (Margaret)',
    'C2',
    'presentations',
    'finance',
    '["Present complex legal and business issues with precision", "Handle challenging questions with diplomatic deflection when appropriate", "Balance transparency with strategic communication"]'::jsonb,
    '[{"term": "fiduciary duty", "definition": "legal obligation to act in the best interest of another party", "example": "Our fiduciary duty requires full disclosure to shareholders."}, {"term": "material exposure", "definition": "significant financial risk that must be disclosed", "example": "The material exposure could exceed $200M in the worst-case scenario."}, {"term": "mitigating factors", "definition": "circumstances that reduce the severity of a situation", "example": "Several mitigating factors work in our favor."}, {"term": "whistleblower provisions", "definition": "protections for employees who report misconduct", "example": "The whistleblower provisions were triggered last quarter."}, {"term": "regulatory settlement", "definition": "agreement with regulators to resolve allegations", "example": "A regulatory settlement might be preferable to prolonged litigation."}]'::jsonb,
    'Thank you for convening this emergency session. Before we begin, I want to assure the board that while the situation is serious, it''s not unprecedented. I''ve prepared a comprehensive brief, but I suspect you''ll have some pointed questions about our compliance framework. Where would you like me to start?',
    20,
    true
);
