-- V28__Increase_roleplay_roles_length.sql
-- Increase length of role columns in roleplay_scenarios table
-- Reason: AI generated role descriptions can exceed 100 characters
-- Date: December 15, 2025

ALTER TABLE roleplay_scenarios 
    ALTER COLUMN your_role TYPE VARCHAR(255),
    ALTER COLUMN ai_role TYPE VARCHAR(255);
