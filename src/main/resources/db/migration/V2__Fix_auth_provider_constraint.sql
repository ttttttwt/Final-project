-- V2__Fix_auth_provider_constraint.sql
-- Fix auth_provider constraint to match Java enum values (uppercase)

-- Drop old constraint
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_auth_provider_check;

-- Add new constraint with uppercase values
ALTER TABLE users ADD CONSTRAINT users_auth_provider_check 
    CHECK (auth_provider IN ('EMAIL', 'GOOGLE', 'FACEBOOK'));

-- Update existing data to uppercase (if any)
UPDATE users SET auth_provider = UPPER(auth_provider) WHERE auth_provider IN ('email', 'google', 'facebook');
