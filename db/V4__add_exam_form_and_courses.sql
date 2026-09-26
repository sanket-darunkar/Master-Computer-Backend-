-- ============================================================
-- Master Computer Academy – Certificate Verification System
-- Migration V4: Add exam_form and courses_list columns to students
--
-- exam_form   – free-text exam form submission status
--               ('Exam Form Submitted' | 'Exam Form Pending')
-- courses_list – pipe-delimited list of enrolled courses
--               e.g. 'DCA|Tally ERP 9|MS-CIT'
--
-- HOW TO APPLY:
--   Paste into the Neon SQL Editor and click Run.
--   Or: psql "$NEON_DATABASE_URL" -f db/V4__add_exam_form_and_courses.sql
--
-- SAFETY:
--   Fully idempotent — uses ALTER TABLE … ADD COLUMN IF NOT EXISTS.
--   Existing rows get exam_form = 'Exam Form Pending' and
--   courses_list = NULL (service will back-fill courses_list = course
--   on first edit).
-- ============================================================

ALTER TABLE students
    ADD COLUMN IF NOT EXISTS exam_form    VARCHAR(50)  NOT NULL DEFAULT 'Exam Form Pending',
    ADD COLUMN IF NOT EXISTS courses_list VARCHAR(2000);

-- Index on exam_form for filtered list queries
CREATE INDEX IF NOT EXISTS idx_student_exam_form ON students (exam_form);

-- ============================================================
-- VERIFY
-- ============================================================
-- SELECT column_name, data_type FROM information_schema.columns
-- WHERE table_name = 'students'
--   AND column_name IN ('exam_form', 'courses_list');
-- ============================================================
