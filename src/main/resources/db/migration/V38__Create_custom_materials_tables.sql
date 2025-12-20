-- V38: Create Custom AI Content Generator tables
-- Feature: User uploads personal documents (PDF, DOCX, Image, URL, Text) 
--          and AI generates personalized learning content.
-- Premium-only feature with 10 materials/month quota.

-- =====================================================
-- ENUMS
-- =====================================================

-- Source type enum for material inputs
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'custom_material_source_type') THEN
        CREATE TYPE custom_material_source_type AS ENUM (
            'PDF', 'DOCX', 'IMAGE', 'YOUTUBE', 'WEBSITE', 'TEXT'
        );
    END IF;
END$$;

-- Material processing status enum
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'custom_material_status') THEN
        CREATE TYPE custom_material_status AS ENUM (
            'PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'
        );
    END IF;
END$$;

-- AI correction mode enum
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'ai_correction_mode') THEN
        CREATE TYPE ai_correction_mode AS ENUM (
            'STRICT', 'POLITE'
        );
    END IF;
END$$;

-- Job status enum  
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'custom_material_job_status') THEN
        CREATE TYPE custom_material_job_status AS ENUM (
            'QUEUED', 'PROCESSING', 'COMPLETED', 'FAILED'
        );
    END IF;
END$$;

-- =====================================================
-- TABLE 1: user_custom_materials (Main table)
-- =====================================================

CREATE TABLE IF NOT EXISTS user_custom_materials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    source_type custom_material_source_type NOT NULL,
    original_file_url TEXT,                    -- Link to ObjectStore (MinIO/S3)
    content_text TEXT,                         -- Extracted raw content (OCR/Transcript)
    input_metadata JSONB DEFAULT '{}'::jsonb,  -- { "pageStart": 1, "pageEnd": 20, "timeStart": 0, "timeEnd": 900 }
    generated_content JSONB DEFAULT '{}'::jsonb, -- { "schemaVersion": 1, "vocabulary": [...], "quiz": [...], ... }
    status custom_material_status DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Add constraint for source type requiring file URL
ALTER TABLE user_custom_materials
    ADD CONSTRAINT chk_source_file_url CHECK (
        (source_type IN ('PDF', 'DOCX', 'IMAGE') AND original_file_url IS NOT NULL)
        OR source_type IN ('YOUTUBE', 'WEBSITE', 'TEXT')
    );

COMMENT ON TABLE user_custom_materials IS 'Stores user-uploaded materials for AI content generation';
COMMENT ON COLUMN user_custom_materials.input_metadata IS 'JSONB: pageStart, pageEnd (for docs) or timeStart, timeEnd (for video) in seconds';
COMMENT ON COLUMN user_custom_materials.generated_content IS 'JSONB with schemaVersion, vocabulary[], quiz[], shadowing[], roleplay{}, summary';

-- =====================================================
-- TABLE 2: user_custom_material_settings
-- =====================================================

CREATE TABLE IF NOT EXISTS user_custom_material_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    material_id UUID NOT NULL REFERENCES user_custom_materials(id) ON DELETE CASCADE,
    target_options JSONB NOT NULL DEFAULT '["VOCABULARY"]'::jsonb, -- ["VOCABULARY", "ROLE_PLAY", "QUIZ", "SUMMARY", "SHADOWING"]
    ai_correction_mode ai_correction_mode DEFAULT 'POLITE',
    style_learn_mode BOOLEAN DEFAULT TRUE,     -- true = include explanations in style transform
    sync_vocab_to_srs BOOLEAN DEFAULT FALSE,   -- true = sync vocabulary to SRS system
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT uq_material_settings_material_id UNIQUE (material_id)
);

COMMENT ON TABLE user_custom_material_settings IS 'Per-material user preferences for AI content generation';

-- =====================================================
-- TABLE 3: custom_material_chat_sessions (Role-Play)
-- =====================================================

CREATE TABLE IF NOT EXISTS custom_material_chat_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    material_id UUID NOT NULL REFERENCES user_custom_materials(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    chat_history JSONB DEFAULT '[]'::jsonb,    -- Array of { role, content, timestamp }
    performance_report JSONB,                   -- End-of-session feedback (for POLITE mode)
    status VARCHAR(20) DEFAULT 'in_progress',   -- in_progress, completed, abandoned
    started_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    ended_at TIMESTAMP WITH TIME ZONE
);

COMMENT ON TABLE custom_material_chat_sessions IS 'Role-play chat sessions linked to custom materials';

-- =====================================================
-- TABLE 4: user_shadowing_attempts
-- =====================================================

CREATE TABLE IF NOT EXISTS user_shadowing_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    material_id UUID NOT NULL REFERENCES user_custom_materials(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sentence_id VARCHAR(50) NOT NULL,          -- ID in generated_content.shadowing
    audio_url TEXT,                            -- Link to user recording file
    score INT CHECK (score >= 0 AND score <= 100),
    feedback JSONB,                            -- Detailed pronunciation feedback
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

COMMENT ON TABLE user_shadowing_attempts IS 'Stores user shadowing practice recordings and scores';

-- =====================================================
-- TABLE 5: custom_material_jobs (Async Processing)
-- =====================================================

CREATE TABLE IF NOT EXISTS custom_material_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    material_id UUID NOT NULL REFERENCES user_custom_materials(id) ON DELETE CASCADE,
    status custom_material_job_status DEFAULT 'QUEUED',
    progress INT DEFAULT 0 CHECK (progress >= 0 AND progress <= 100),
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    last_error TEXT,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    CONSTRAINT uq_material_jobs_material_id UNIQUE (material_id)
);

COMMENT ON TABLE custom_material_jobs IS 'Tracks async processing jobs for durability and recovery';

-- =====================================================
-- INDEXES
-- =====================================================

-- user_custom_materials indexes
CREATE INDEX IF NOT EXISTS idx_custom_materials_user_id 
    ON user_custom_materials(user_id);
CREATE INDEX IF NOT EXISTS idx_custom_materials_status 
    ON user_custom_materials(status);
CREATE INDEX IF NOT EXISTS idx_custom_materials_user_status 
    ON user_custom_materials(user_id, status);
CREATE INDEX IF NOT EXISTS idx_custom_materials_created_at 
    ON user_custom_materials(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_custom_materials_user_created 
    ON user_custom_materials(user_id, created_at DESC);

-- custom_material_chat_sessions indexes
CREATE INDEX IF NOT EXISTS idx_chat_sessions_material_id 
    ON custom_material_chat_sessions(material_id);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_user_id 
    ON custom_material_chat_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_status 
    ON custom_material_chat_sessions(status);

-- user_shadowing_attempts indexes
CREATE INDEX IF NOT EXISTS idx_shadowing_attempts_material_id 
    ON user_shadowing_attempts(material_id);
CREATE INDEX IF NOT EXISTS idx_shadowing_attempts_user_id 
    ON user_shadowing_attempts(user_id);
CREATE INDEX IF NOT EXISTS idx_shadowing_attempts_user_material 
    ON user_shadowing_attempts(user_id, material_id);

-- custom_material_jobs indexes
CREATE INDEX IF NOT EXISTS idx_material_jobs_status 
    ON custom_material_jobs(status);
CREATE INDEX IF NOT EXISTS idx_material_jobs_queued 
    ON custom_material_jobs(status, created_at) 
    WHERE status = 'QUEUED';

-- =====================================================
-- HELPER FUNCTIONS
-- =====================================================

-- Function to count user's materials in current month (for quota)
CREATE OR REPLACE FUNCTION count_user_monthly_materials(p_user_id UUID)
RETURNS INT AS $$
DECLARE
    material_count INT;
BEGIN
    SELECT COUNT(*) INTO material_count
    FROM user_custom_materials
    WHERE user_id = p_user_id
      AND created_at >= date_trunc('month', CURRENT_TIMESTAMP);
    
    RETURN COALESCE(material_count, 0);
END;
$$ LANGUAGE plpgsql;

-- Function to check if user can create new material (quota: 10/month)
CREATE OR REPLACE FUNCTION can_create_material(p_user_id UUID, p_monthly_limit INT DEFAULT 10)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN count_user_monthly_materials(p_user_id) < p_monthly_limit;
END;
$$ LANGUAGE plpgsql;

-- Function to update material status when job status changes
CREATE OR REPLACE FUNCTION sync_material_status()
RETURNS TRIGGER AS $$
BEGIN
    -- Mirror job status to material status
    IF NEW.status = 'COMPLETED' THEN
        UPDATE user_custom_materials 
        SET status = 'COMPLETED', updated_at = NOW()
        WHERE id = NEW.material_id;
    ELSIF NEW.status = 'FAILED' THEN
        UPDATE user_custom_materials 
        SET status = 'FAILED', error_message = NEW.last_error, updated_at = NOW()
        WHERE id = NEW.material_id;
    ELSIF NEW.status = 'PROCESSING' THEN
        UPDATE user_custom_materials 
        SET status = 'PROCESSING', updated_at = NOW()
        WHERE id = NEW.material_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to sync job status to material status
DROP TRIGGER IF EXISTS trg_sync_material_status ON custom_material_jobs;
CREATE TRIGGER trg_sync_material_status
    AFTER UPDATE OF status ON custom_material_jobs
    FOR EACH ROW
    WHEN (OLD.status IS DISTINCT FROM NEW.status)
    EXECUTE FUNCTION sync_material_status();

-- =====================================================
-- UPDATE user_ai_quotas TABLE (add custom_materials quota)
-- =====================================================

-- Add monthly custom materials quota column if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'user_ai_quotas' AND column_name = 'custom_materials_monthly_limit'
    ) THEN
        ALTER TABLE user_ai_quotas ADD COLUMN custom_materials_monthly_limit INT DEFAULT 10;
        ALTER TABLE user_ai_quotas ADD COLUMN custom_materials_used_this_month INT DEFAULT 0;
        ALTER TABLE user_ai_quotas ADD COLUMN custom_materials_month_reset_at TIMESTAMP WITH TIME ZONE;
    END IF;
END$$;

COMMENT ON COLUMN user_ai_quotas.custom_materials_monthly_limit IS 'Max custom materials per month (Premium: 10)';
COMMENT ON COLUMN user_ai_quotas.custom_materials_used_this_month IS 'Custom materials created this month';
