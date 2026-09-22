-- ============================================================
-- Master Computer Academy – Certificate Verification System
-- Migration V2: Add binary photo storage to certificates table
--
-- HOW TO APPLY:
--   Paste into the Neon SQL Editor and click Run.
--   Or: psql "$NEON_DATABASE_URL" -f db/V2__add_photo_storage.sql
--
-- SAFETY:
--   - Adds two nullable columns — zero impact on existing rows.
--   - Does NOT drop or modify any existing column.
--   - Existing certificates keep their student_photo_url if set.
--   - Idempotent via IF NOT EXISTS column check (PostgreSQL 9.6+).
--
-- COLUMNS ADDED:
--   photo_data      BYTEA   – raw bytes of the uploaded photo (max 2 MB enforced in app)
--   photo_mime_type VARCHAR – e.g. "image/jpeg", "image/png"
-- ============================================================

-- Add photo_data column (nullable BYTEA — null = no photo uploaded yet)
ALTER TABLE certificates
    ADD COLUMN IF NOT EXISTS photo_data BYTEA;

-- Add photo_mime_type column (nullable VARCHAR — set whenever photo_data is non-null)
ALTER TABLE certificates
    ADD COLUMN IF NOT EXISTS photo_mime_type VARCHAR(20);

-- ============================================================
-- VERIFY (run after applying to confirm columns exist)
-- ============================================================
-- SELECT column_name, data_type
-- FROM   information_schema.columns
-- WHERE  table_name = 'certificates'
--   AND  column_name IN ('photo_data', 'photo_mime_type');
--
-- Expected:
--   photo_data       | bytea
--   photo_mime_type  | character varying
-- ============================================================
