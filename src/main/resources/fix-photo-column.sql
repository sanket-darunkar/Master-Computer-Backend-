-- Fix photo_data column type: drop oid column and recreate as bytea.
-- This runs once via spring.sql.init on startup (prod profile only).
-- Safe to run multiple times — IF EXISTS guards prevent errors if column
-- is already correct or doesn't exist.

-- Drop the incorrectly-typed oid column if it exists
ALTER TABLE certificates DROP COLUMN IF EXISTS photo_data;
ALTER TABLE certificates DROP COLUMN IF EXISTS photo_mime_type;
ALTER TABLE certificates DROP COLUMN IF EXISTS student_photo_url;

-- Recreate with correct types (Hibernate ddl-auto=update will also do this,
-- but we do it here explicitly to ensure bytea, not oid)
ALTER TABLE certificates ADD COLUMN IF NOT EXISTS photo_data      BYTEA;
ALTER TABLE certificates ADD COLUMN IF NOT EXISTS photo_mime_type VARCHAR(20);
ALTER TABLE certificates ADD COLUMN IF NOT EXISTS student_photo_url VARCHAR(1024);
