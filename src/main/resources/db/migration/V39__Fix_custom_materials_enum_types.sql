-- V39: Fix PostgreSQL enum types to VARCHAR for JPA compatibility
-- JPA @Enumerated(EnumType.STRING) uses VARCHAR, but V38 created PostgreSQL custom ENUM types.
-- This migration changes the columns to VARCHAR to work with Hibernate.

-- =====================================================
-- 1. Drop dependencies (Triggers, Functions, Indices)
-- =====================================================

DROP TRIGGER IF EXISTS trg_sync_material_status ON custom_material_jobs;
DROP FUNCTION IF EXISTS sync_material_status();

-- Drop indices that rely on the enum columns, especially partial indices
DROP INDEX IF EXISTS idx_material_jobs_queued;
DROP INDEX IF EXISTS idx_material_jobs_status;
DROP INDEX IF EXISTS idx_custom_materials_status;
DROP INDEX IF EXISTS idx_custom_materials_user_status;

-- =====================================================
-- 2. Alter user_custom_materials.source_type
-- =====================================================

-- Drop the constraint first (it references source_type)
ALTER TABLE user_custom_materials DROP CONSTRAINT IF EXISTS chk_source_file_url;

-- Change source_type from custom_material_source_type to VARCHAR
ALTER TABLE user_custom_materials 
    ALTER COLUMN source_type DROP DEFAULT;
ALTER TABLE user_custom_materials 
    ALTER COLUMN source_type TYPE VARCHAR(20) USING source_type::text;

-- Re-add the constraint
ALTER TABLE user_custom_materials
    ADD CONSTRAINT chk_source_file_url CHECK (
        (source_type IN ('PDF', 'DOCX', 'IMAGE') AND original_file_url IS NOT NULL)
        OR source_type IN ('YOUTUBE', 'WEBSITE', 'TEXT')
    );

-- =====================================================
-- 3. Alter user_custom_materials.status
-- =====================================================

ALTER TABLE user_custom_materials 
    ALTER COLUMN status DROP DEFAULT;
ALTER TABLE user_custom_materials 
    ALTER COLUMN status TYPE VARCHAR(20) USING status::text;
ALTER TABLE user_custom_materials 
    ALTER COLUMN status SET DEFAULT 'PENDING';

-- =====================================================
-- 4. Alter user_custom_material_settings.ai_correction_mode
-- =====================================================

ALTER TABLE user_custom_material_settings 
    ALTER COLUMN ai_correction_mode DROP DEFAULT;
ALTER TABLE user_custom_material_settings 
    ALTER COLUMN ai_correction_mode TYPE VARCHAR(20) USING ai_correction_mode::text;
ALTER TABLE user_custom_material_settings 
    ALTER COLUMN ai_correction_mode SET DEFAULT 'POLITE';

-- =====================================================
-- 5. Alter custom_material_jobs.status
-- =====================================================

ALTER TABLE custom_material_jobs 
    ALTER COLUMN status DROP DEFAULT;
ALTER TABLE custom_material_jobs 
    ALTER COLUMN status TYPE VARCHAR(20) USING status::text;
ALTER TABLE custom_material_jobs 
    ALTER COLUMN status SET DEFAULT 'QUEUED';

-- =====================================================
-- 6. Recreate Indices
-- =====================================================

CREATE INDEX idx_custom_materials_status ON user_custom_materials(status);
CREATE INDEX idx_custom_materials_user_status ON user_custom_materials(user_id, status);
CREATE INDEX idx_material_jobs_status ON custom_material_jobs(status);
CREATE INDEX idx_material_jobs_queued ON custom_material_jobs(status, created_at) WHERE status = 'QUEUED';

-- =====================================================
-- 7. Recreate Function and Trigger
-- =====================================================

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

CREATE TRIGGER trg_sync_material_status
    AFTER UPDATE OF status ON custom_material_jobs
    FOR EACH ROW
    WHEN (OLD.status IS DISTINCT FROM NEW.status)
    EXECUTE FUNCTION sync_material_status();
