-- V49: Add generate_flashcard_images column to user_custom_material_settings
-- This allows users to optionally generate AI images for synced flashcards

ALTER TABLE user_custom_material_settings 
ADD COLUMN generate_flashcard_images BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN user_custom_material_settings.generate_flashcard_images IS 'If true, generate AI images for vocabulary when syncing to Flashcard SRS';
