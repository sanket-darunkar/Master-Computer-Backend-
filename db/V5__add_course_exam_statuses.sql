-- ============================================================
-- Master Computer Academy – Certificate Verification System
-- Migration V5: Add per-course exam form status column
--
-- course_exam_statuses – pipe-delimited list of exam form statuses,
--   one entry per course in courses_list, positionally aligned.
--   e.g. courses_list          = 'DCA|Tally ERP 9|MS-CIT'
--        course_exam_statuses  = 'Exam Form Submitted|Exam Form Pending|Exam Form Pending'
--
-- Back-fill: existing rows get their current exam_form value copied
--   into every position of course_exam_statuses so no data is lost.
--   Students with a single course:  course_exam_statuses = exam_form
--   Students with multiple courses: each position gets exam_form value
--   Students with NULL courses_list: course_exam_statuses = exam_form
--
-- The legacy exam_form column is KEPT intact for backward compatibility.
-- It continues to reflect the "overall" status (= Submitted only when ALL
-- courses are Submitted, otherwise Pending). The application layer keeps
-- them in sync on every write.
--
-- HOW TO APPLY:
--   Paste into the Neon SQL Editor and click Run.
--   Or: psql "$NEON_DATABASE_URL" -f db/V5__add_course_exam_statuses.sql
--
-- SAFETY:
--   Fully idempotent — uses ADD COLUMN IF NOT EXISTS.
--   Back-fill is a single UPDATE; safe to run more than once because
--   WHERE clause skips rows that already have a value.
-- ============================================================

-- 1. Add the new column (nullable so back-fill can run separately)
ALTER TABLE students
    ADD COLUMN IF NOT EXISTS course_exam_statuses VARCHAR(2000);

-- 2. Back-fill: for each existing student, replicate their current exam_form
--    across every course slot.
--
--    Logic:
--      If courses_list is NULL or empty → single slot = exam_form value
--      Otherwise → replace each pipe-segment with the exam_form value
--
--    We use a regexp_replace to swap every pipe-delimited segment with
--    the existing exam_form value, preserving the number of pipes (= courses).
--
UPDATE students
SET course_exam_statuses =
    CASE
        -- No courses_list yet (legacy single-course row): just one slot
        WHEN courses_list IS NULL OR courses_list = ''
            THEN exam_form

        -- Multiple courses: generate N copies of exam_form joined by '|'
        -- where N = number of pipes + 1
        ELSE (
            SELECT string_agg(exam_form, '|')
            FROM generate_series(
                1,
                array_length(string_to_array(courses_list, '|'), 1)
            )
        )
    END
WHERE course_exam_statuses IS NULL;

-- 3. Index for any future query on this column (optional but cheap)
CREATE INDEX IF NOT EXISTS idx_student_course_exam_statuses
    ON students USING gin (string_to_array(course_exam_statuses, '|'));

-- ============================================================
-- VERIFY
-- ============================================================
-- SELECT student_id, courses_list, exam_form, course_exam_statuses
-- FROM   students
-- LIMIT  10;
--
-- Expected: course_exam_statuses has one pipe-delimited value per
--           course in courses_list, matching the original exam_form.
-- ============================================================
