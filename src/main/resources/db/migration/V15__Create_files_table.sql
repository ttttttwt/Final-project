-- V15__Create_files_table.sql
-- File upload system - stores metadata for uploaded files

CREATE TABLE IF NOT EXISTS files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_filename VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL UNIQUE,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    uploaded_by UUID REFERENCES users(id) ON DELETE SET NULL,
    uploaded_at TIMESTAMPTZ DEFAULT NOW(),

    -- Optional metadata
    width INTEGER,           -- For images
    height INTEGER,          -- For images
    duration_seconds INTEGER, -- For audio

    -- Access control
    is_public BOOLEAN DEFAULT false,
    access_count INTEGER DEFAULT 0,
    last_accessed_at TIMESTAMPTZ,

    CONSTRAINT valid_category CHECK (category IN (
        'AVATAR', 'COURSE_THUMBNAIL', 'LESSON_AUDIO',
        'LESSON_IMAGE', 'DOCUMENT', 'CERTIFICATE'
    )),
    CONSTRAINT valid_file_size CHECK (file_size > 0 AND file_size <= 52428800) -- 50MB max
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_files_uploaded_by ON files(uploaded_by);
CREATE INDEX IF NOT EXISTS idx_files_category ON files(category);
CREATE INDEX IF NOT EXISTS idx_files_uploaded_at ON files(uploaded_at DESC);

COMMENT ON TABLE files IS 'Metadata for uploaded files';
COMMENT ON COLUMN files.storage_path IS 'Relative path from upload root directory';
COMMENT ON COLUMN files.is_public IS 'If true, file can be accessed without authentication';
