-- ============================================================
-- Master Computer Academy – Certificate Verification System
-- Migration V3: Add students table
--
-- HOW TO APPLY:
--   Paste into the Neon SQL Editor and click Run.
--   Or: psql "$NEON_DATABASE_URL" -f db/V3__add_students_table.sql
--
-- SAFETY:
--   - Fully idempotent: uses CREATE TABLE IF NOT EXISTS throughout.
--   - Does NOT touch existing tables (admin_users, certificates, verification_logs).
--   - Safe to run more than once.
-- ============================================================

CREATE TABLE IF NOT EXISTS students (
    id                BIGSERIAL     NOT NULL,

    -- Identity
    student_id        VARCHAR(50)   NOT NULL,

    -- Personal
    first_name        VARCHAR(100)  NOT NULL,
    middle_name       VARCHAR(100),
    surname           VARCHAR(100)  NOT NULL,
    applicant_name    VARCHAR(255),
    mother_name       VARCHAR(255),
    date_of_birth     DATE,
    gender            VARCHAR(20),
    marital_status    VARCHAR(20),
    aadhaar_number    VARCHAR(20),

    -- Contact
    own_mobile        VARCHAR(15)   NOT NULL,
    other_mobile      VARCHAR(15),

    -- Address
    house_no          VARCHAR(50),
    street            VARCHAR(255),
    city              VARCHAR(100),
    tahsil            VARCHAR(100),
    district          VARCHAR(100),
    pin_code          VARCHAR(10),

    -- Academic
    qualification     VARCHAR(50),
    category          VARCHAR(20),

    -- Course / Admission
    course            VARCHAR(255)  NOT NULL,
    admission_date    DATE          NOT NULL,
    course_duration   VARCHAR(50),
    batch_time        VARCHAR(50),

    -- Fees
    total_fees        NUMERIC(10,2),
    fees_paid         NUMERIC(10,2),
    receipt_number    VARCHAR(50),
    receipt_date      DATE,

    -- Misc
    notes             VARCHAR(2000),

    -- Status (matches StudentStatus enum: ACTIVE|INACTIVE|COMPLETED|DROPPED)
    status            VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',

    -- Photo (binary, max 2 MB enforced at application layer)
    photo_data        BYTEA,
    photo_mime_type   VARCHAR(20),

    -- Audit
    created_at        TIMESTAMP     NOT NULL,
    updated_at        TIMESTAMP     NOT NULL,

    CONSTRAINT pk_students           PRIMARY KEY (id),
    CONSTRAINT uq_students_id        UNIQUE      (student_id),
    CONSTRAINT chk_students_status   CHECK       (status IN ('ACTIVE','INACTIVE','COMPLETED','DROPPED'))
);

-- Indexes matching JPA @Index declarations on the Student entity
CREATE UNIQUE INDEX IF NOT EXISTS idx_student_student_id
    ON students (student_id);

CREATE INDEX IF NOT EXISTS idx_student_own_mobile
    ON students (own_mobile);

CREATE INDEX IF NOT EXISTS idx_student_first_name
    ON students (first_name);

CREATE INDEX IF NOT EXISTS idx_student_surname
    ON students (surname);

CREATE INDEX IF NOT EXISTS idx_student_course
    ON students (course);

CREATE INDEX IF NOT EXISTS idx_student_status
    ON students (status);

CREATE INDEX IF NOT EXISTS idx_student_admission_dt
    ON students (admission_date);

-- ============================================================
-- VERIFY (run after applying)
-- ============================================================
-- SELECT table_name FROM information_schema.tables
-- WHERE  table_schema = 'public' AND table_name = 'students';
--
-- Expected: students
-- ============================================================
