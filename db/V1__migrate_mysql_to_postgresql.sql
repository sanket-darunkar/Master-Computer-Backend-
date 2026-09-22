-- ============================================================
-- Master Computer Academy – Certificate Verification System
-- PostgreSQL Migration Script
-- Target: Neon PostgreSQL
--
-- Generated from local MySQL (mca_cert_dev) schema inspection.
-- Matches JPA entities exactly so Hibernate ddl-auto=validate passes.
--
-- HOW TO APPLY:
--   Paste the entire contents of this file into the Neon SQL Editor
--   (Neon dashboard → your project → SQL Editor) and click Run.
--
--   Alternatively, if psql is available:
--     psql "$NEON_DATABASE_URL" -f db/V1__migrate_mysql_to_postgresql.sql
--
-- SAFETY RULES:
--   - This script is IDEMPOTENT: safe to run more than once.
--   - Uses IF NOT EXISTS / ON CONFLICT DO NOTHING throughout.
--   - Does NOT drop any existing tables.
--   - Does NOT contain any passwords or secrets.
--   - The admin password below is a BCrypt hash (cost 12), not a
--     plaintext password. BCrypt hashes are safe to store in SQL files.
--
-- MySQL → PostgreSQL differences handled:
--   - BIGINT AUTO_INCREMENT  → BIGSERIAL (maps to GenerationType.IDENTITY)
--   - datetime(6)            → TIMESTAMP (microsecond precision)
--   - enum('A','B')          → VARCHAR(N)  (@Enumerated(STRING) in JPA)
--   - utf8mb4 collation      → default UTF-8 (PostgreSQL default)
--   - ENGINE=InnoDB          → not applicable in PostgreSQL
-- ============================================================


-- ============================================================
-- TABLE: admin_users
-- ============================================================
CREATE TABLE IF NOT EXISTS admin_users (
    id         BIGSERIAL    NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    CONSTRAINT pk_admin_users         PRIMARY KEY (id),
    CONSTRAINT uq_admin_users_email   UNIQUE      (email),
    CONSTRAINT chk_admin_users_role   CHECK       (role IN ('ADMIN'))
);

-- Index on email (also covered by the unique constraint above,
-- but named explicitly to match Hibernate's @Index declaration)
CREATE UNIQUE INDEX IF NOT EXISTS idx_admin_email
    ON admin_users (email);


-- ============================================================
-- TABLE: certificates
-- ============================================================
CREATE TABLE IF NOT EXISTS certificates (
    id                 BIGSERIAL     NOT NULL,
    certificate_number VARCHAR(100)  NOT NULL,
    student_name       VARCHAR(255)  NOT NULL,
    student_photo_url  VARCHAR(1024)     NULL,
    course_name        VARCHAR(255)  NOT NULL,
    issue_date         DATE          NOT NULL,
    duration           VARCHAR(100)      NULL,
    institution_name   VARCHAR(255)      NULL,
    marks              VARCHAR(50)       NULL,
    grade              VARCHAR(10)       NULL,
    -- status stored as VARCHAR to match @Enumerated(EnumType.STRING)
    -- Valid values enforced by CHECK: ACTIVE, REVOKED, PENDING
    status             VARCHAR(20)   NOT NULL,
    created_at         TIMESTAMP     NOT NULL,
    updated_at         TIMESTAMP     NOT NULL,
    CONSTRAINT pk_certificates              PRIMARY KEY (id),
    CONSTRAINT uq_certificates_cert_number  UNIQUE      (certificate_number),
    CONSTRAINT chk_certificates_status      CHECK       (status IN ('ACTIVE','REVOKED','PENDING'))
);

-- Indexes matching JPA @Index declarations on the Certificate entity
CREATE UNIQUE INDEX IF NOT EXISTS idx_cert_number
    ON certificates (certificate_number);

CREATE INDEX IF NOT EXISTS idx_cert_student_name
    ON certificates (student_name);

CREATE INDEX IF NOT EXISTS idx_cert_course_name
    ON certificates (course_name);

CREATE INDEX IF NOT EXISTS idx_cert_status
    ON certificates (status);


-- ============================================================
-- TABLE: verification_logs
-- ============================================================
CREATE TABLE IF NOT EXISTS verification_logs (
    id             BIGSERIAL  NOT NULL,
    certificate_id BIGINT     NOT NULL,
    verified_at    TIMESTAMP  NOT NULL,
    CONSTRAINT pk_verification_logs  PRIMARY KEY (id),
    CONSTRAINT fk_vlog_certificate   FOREIGN KEY (certificate_id)
                                     REFERENCES  certificates (id)
);

-- Indexes matching JPA @Index declarations on the VerificationLog entity
CREATE INDEX IF NOT EXISTS idx_vlog_certificate_id
    ON verification_logs (certificate_id);

CREATE INDEX IF NOT EXISTS idx_vlog_verified_at
    ON verification_logs (verified_at);


-- ============================================================
-- SEQUENCES: advance SERIAL sequences past existing MySQL IDs
-- so that new records do not collide with migrated ones.
--
-- MySQL admin_users  AUTO_INCREMENT was 2  (1 row with id=1)
-- MySQL certificates AUTO_INCREMENT was 3  (2 rows, ids 1–2)
-- MySQL verification_logs AUTO_INCREMENT was 18 (17 rows, ids 1–17)
-- ============================================================
SELECT setval('admin_users_id_seq',        1,  true);
SELECT setval('certificates_id_seq',       2,  true);
SELECT setval('verification_logs_id_seq',  17, true);


-- ============================================================
-- DATA: admin_users  (1 row)
--
-- The password column contains a BCrypt hash (cost 12).
-- BCrypt hashes are NOT secrets – they are designed to be
-- stored and are computationally infeasible to reverse.
-- ============================================================
INSERT INTO admin_users (id, email, password, role, created_at, updated_at)
VALUES (
    1,
    'admin@mastercomputeracademy.com',
    '$2a$12$u1RF1Q0kIjT23TawnZfM0.rZo2Bt1FjqP42OvgTW2f4Yi/GOo.DTu',
    'ADMIN',
    '2026-09-20 14:46:25.183220',
    '2026-09-20 14:46:25.183220'
)
ON CONFLICT (id) DO NOTHING;


-- ============================================================
-- DATA: certificates  (2 rows)
-- ============================================================
INSERT INTO certificates (
    id, certificate_number, student_name, student_photo_url,
    course_name, issue_date, duration, institution_name,
    marks, grade, status, created_at, updated_at
) VALUES
(
    1,
    'MCA-2024-001',
    'Rahul Sharma',
    NULL,
    'Diploma in Computer Application',
    '2024-03-15',
    '6 Months',
    'Master Computer Academy',
    '450/500',
    'A+',
    'ACTIVE',
    '2026-09-20 15:56:36.372217',
    '2026-09-20 15:56:36.372217'
),
(
    2,
    'MCA-2025-001',
    'Sanket Darunkar',
    NULL,
    'MS-CIT',
    '2026-09-20',
    '3 months',
    'Master Computer',
    '99',
    'A+',
    'ACTIVE',
    '2026-09-20 18:34:58.684386',
    '2026-09-20 18:34:58.684386'
)
ON CONFLICT (id) DO NOTHING;


-- ============================================================
-- DATA: verification_logs  (17 rows)
-- ============================================================
INSERT INTO verification_logs (id, certificate_id, verified_at) VALUES
 (1,  1, '2026-09-20 15:56:36.441210'),
 (2,  1, '2026-09-20 15:56:59.325704'),
 (3,  1, '2026-09-20 16:01:47.281746'),
 (4,  1, '2026-09-20 16:05:01.909929'),
 (5,  1, '2026-09-20 16:05:36.354886'),
 (6,  1, '2026-09-20 16:06:33.478101'),
 (7,  1, '2026-09-20 18:14:56.643959'),
 (8,  1, '2026-09-20 18:17:27.623475'),
 (9,  1, '2026-09-20 18:27:18.929212'),
(10,  2, '2026-09-20 18:35:21.381614'),
(11,  1, '2026-09-20 18:36:18.007924'),
(12,  2, '2026-09-20 18:57:17.096224'),
(13,  1, '2026-09-20 19:05:15.297267'),
(14,  2, '2026-09-20 21:38:36.804202'),
(15,  1, '2026-09-20 21:45:27.364080'),
(16,  2, '2026-09-20 21:46:54.584352'),
(17,  1, '2026-09-20 21:47:02.598352')
ON CONFLICT (id) DO NOTHING;


-- ============================================================
-- VERIFY (run manually after import to confirm row counts)
-- ============================================================
-- SELECT 'admin_users'       AS tbl, COUNT(*) AS rows FROM admin_users
-- UNION ALL
-- SELECT 'certificates',             COUNT(*)          FROM certificates
-- UNION ALL
-- SELECT 'verification_logs',        COUNT(*)          FROM verification_logs;
--
-- Expected:
--   admin_users       | 1
--   certificates      | 2
--   verification_logs | 17
-- ============================================================
