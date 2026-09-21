# Master Computer Academy — Certificate Verification System

Backend REST API for the **Master Computer Academy Certificate Verification System**.  
Students and employers can verify certificates online; academy admins can manage the full certificate lifecycle securely.

**Academy**  
Wathoda Layout, Lok Kalyan Society, Anmol Nagar, Dighori, Nagpur, Maharashtra 440034  
Phone: 9156348591

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Tech Stack](#2-tech-stack)
3. [Requirements](#3-requirements)
4. [MySQL Setup](#4-mysql-setup)
5. [Environment Variables](#5-environment-variables)
6. [How to Run Locally](#6-how-to-run-locally)
7. [API Endpoints](#7-api-endpoints)
8. [Admin Authentication](#8-admin-authentication)
9. [Database Structure](#9-database-structure)
10. [Testing](#10-testing)
11. [Production Considerations](#11-production-considerations)

---

## 1. Project Overview

The backend provides two primary capabilities:

| Actor | Capability |
|---|---|
| **Public user** (student / employer) | Verify a certificate by its unique certificate number — no login required |
| **Admin** | Login · Add · View · Search · Edit · Revoke certificates · View verification history |

Every public verification is recorded in an audit log. Certificates are never physically deleted — they are revoked instead, preserving the historical record.

The architecture is ready for QR code verification: QR codes on printed certificates link to `https://YOUR-DOMAIN/certificate-verification?certificate={certificateNumber}` and the frontend calls `GET /api/certificates/verify/{certificateNumber}` to display the result.

---

## 2. Tech Stack

| Component | Version |
|---|---|
| Java | 17 (compiled with Java 21 runtime) |
| Spring Boot | 3.2.5 |
| Spring Security | 6.2.x |
| Spring Data JPA | 3.2.x |
| Hibernate | 6.4.x |
| MySQL Connector/J | 8.3.0 |
| JJWT (JWT) | 0.12.5 |
| SpringDoc OpenAPI / Swagger UI | 2.5.0 |
| Lombok | via Spring Boot BOM |
| Maven | 3.9.x |
| Test DB | H2 (in-memory, `MODE=MySQL`) |

---

## 3. Requirements

- **Java 17+** (Java 21 also works)
- **Maven 3.8+**
- **MySQL 8.0+** (for dev/prod; tests use H2)
- A terminal and your favourite HTTP client (curl, Postman, Bruno, etc.)

---

## 4. MySQL Setup

```sql
-- Run these commands in your MySQL client once
CREATE DATABASE mca_cert_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create a dedicated user (recommended — don't use root in production)
CREATE USER 'mca_user'@'localhost' IDENTIFIED BY 'StrongPassword@123';
GRANT ALL PRIVILEGES ON mca_cert_dev.* TO 'mca_user'@'localhost';
FLUSH PRIVILEGES;
```

Hibernate will create all tables automatically on first startup (`ddl-auto=update` in dev).

For **production**, create the production database the same way (different name), set `SPRING_PROFILES_ACTIVE=prod`, and use a proper schema migration tool like Flyway.

---

## 5. Environment Variables

Copy `.env.example` to `.env` — **never commit `.env` to version control**.

| Variable | Required | Description | Dev Default |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | No | Active profile: `dev` or `prod` | `dev` |
| `SERVER_PORT` | No | HTTP port | `8080` |
| `DB_URL` | Yes | JDBC connection string | `jdbc:mysql://localhost:3306/mca_cert_dev?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true` |
| `DB_USERNAME` | Yes | Database user | `root` |
| `DB_PASSWORD` | Yes | Database password | *(empty)* |
| `JWT_SECRET` | **Yes** | Base64-encoded 256-bit HMAC secret | — |
| `JWT_EXPIRATION_MS` | No | Token lifetime in milliseconds | `86400000` (24 h) |
| `CORS_ALLOWED_ORIGIN` | No | Frontend origin allowed by CORS | `http://localhost:5173` |
| `ADMIN_EMAIL` | No | Seed admin email (dev only) | `admin@mastercomputer.local` |
| `ADMIN_PASSWORD` | No | Seed admin password (dev only) | `ChangeMe@123` |

### Generating a JWT secret

```bash
openssl rand -base64 32
```

Paste the output as the value of `JWT_SECRET`. The key must decode to at least 32 bytes (256 bits) for HS256.

---

## 6. How to Run Locally

### Option A — Maven (recommended for development)

```bash
# 1. Clone the repository
git clone <repo-url>
cd master-computer-academy-backend

# 2. Set environment variables (one-time)
export DB_URL="jdbc:mysql://localhost:3306/mca_cert_dev?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true"
export DB_USERNAME="mca_user"
export DB_PASSWORD="StrongPassword@123"
export JWT_SECRET="$(openssl rand -base64 32)"
export ADMIN_EMAIL="admin@mastercomputeracademy.com"
export ADMIN_PASSWORD="YourStrongAdminPassword@1"

# 3. Run
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.  
On the first start, the seed admin account is created automatically (dev profile only).

### Option B — Packaged JAR

```bash
# Build
mvn clean package -DskipTests

# Run (all env vars must already be exported or passed inline)
java -jar target/certificate-verification-1.0.0.jar
```

### Swagger UI (dev only)

Open `http://localhost:8080/swagger-ui.html` in your browser.  
Swagger is **disabled** on the prod profile.

---

## 7. API Endpoints

### Public — no authentication required

#### Health check

```
GET /api/health
```

Response:
```json
{ "status": "UP" }
```

---

#### Verify a certificate

```
GET /api/certificates/verify/{certificateNumber}
```

**Example — certificate found (ACTIVE)**

Request:
```bash
curl http://localhost:8080/api/certificates/verify/MCA-2024-001
```

Response `200 OK`:
```json
{
  "success": true,
  "message": "Certificate found",
  "data": {
    "certificateNumber": "MCA-2024-001",
    "studentName": "Rahul Sharma",
    "studentPhotoUrl": "https://storage.example.com/photos/rahul.jpg",
    "courseName": "Diploma in Computer Application",
    "issueDate": "2024-03-15",
    "duration": "6 Months",
    "institutionName": "Master Computer Academy",
    "marks": "450/500",
    "grade": "A+",
    "status": "ACTIVE"
  },
  "timestamp": "2024-09-20T14:00:00"
}
```

**Certificate not found** — `404 Not Found`:
```json
{
  "success": false,
  "message": "Certificate not found: INVALID-999",
  "timestamp": "2024-09-20T14:00:00"
}
```

**Revoked certificate** — `200 OK` with `status: "REVOKED"`:
```json
{
  "success": true,
  "message": "Certificate found",
  "data": {
    "certificateNumber": "MCA-2024-002",
    "studentName": "Priya Patel",
    "courseName": "Tally with GST",
    "status": "REVOKED"
  },
  "timestamp": "2024-09-20T14:00:00"
}
```

---

### Admin — JWT Bearer token required

All admin endpoints require the header:
```
Authorization: Bearer <token>
```

---

#### Login

```
POST /api/admin/auth/login
```

Request:
```json
{
  "email": "admin@mastercomputeracademy.com",
  "password": "YourStrongAdminPassword@1"
}
```

Response `200 OK`:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer"
  },
  "timestamp": "2024-09-20T14:00:00"
}
```

---

#### Create certificate

```
POST /api/admin/certificates
Authorization: Bearer <token>
```

Request:
```json
{
  "certificateNumber": "MCA-2024-003",
  "studentName": "Amit Kumar",
  "studentPhotoUrl": "https://storage.example.com/photos/amit.jpg",
  "courseName": "MS Office",
  "issueDate": "2024-05-20",
  "duration": "3 Months",
  "institutionName": "Master Computer Academy",
  "marks": "95/100",
  "grade": "A+"
}
```

Response `201 Created`:
```json
{
  "success": true,
  "message": "Certificate created successfully",
  "data": {
    "id": 3,
    "certificateNumber": "MCA-2024-003",
    "studentName": "Amit Kumar",
    "courseName": "MS Office",
    "issueDate": "2024-05-20",
    "duration": "3 Months",
    "institutionName": "Master Computer Academy",
    "marks": "95/100",
    "grade": "A+",
    "status": "ACTIVE",
    "createdAt": "2024-09-20T14:00:00",
    "updatedAt": "2024-09-20T14:00:00"
  },
  "timestamp": "2024-09-20T14:00:00"
}
```

---

#### List / search certificates

```
GET /api/admin/certificates?page=0&size=10&search=Rahul&status=ACTIVE&course=Diploma
Authorization: Bearer <token>
```

| Param | Type | Required | Description |
|---|---|---|---|
| `page` | int | No | Page number, 0-indexed (default `0`) |
| `size` | int | No | Page size, max 100 (default `10`) |
| `search` | string | No | Matches certificate number, student name, or course name |
| `status` | string | No | `ACTIVE`, `REVOKED`, or `PENDING` |
| `course` | string | No | Partial course name match |

Response `200 OK`:
```json
{
  "success": true,
  "message": "Certificates retrieved",
  "data": {
    "content": [ { "id": 1, "certificateNumber": "MCA-2024-001", "..." : "..." } ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

---

#### Get certificate by ID

```
GET /api/admin/certificates/{id}
Authorization: Bearer <token>
```

Response `200 OK` — same shape as the create response.  
Response `404` if not found.

---

#### Update certificate

```
PUT /api/admin/certificates/{id}
Authorization: Bearer <token>
```

All editable fields must be supplied. The `certificateNumber` field **cannot** be changed after creation.

```json
{
  "studentName": "Rahul Sharma",
  "studentPhotoUrl": "https://storage.example.com/photos/rahul-v2.jpg",
  "courseName": "Advanced Java",
  "issueDate": "2024-06-01",
  "duration": "6 Months",
  "institutionName": "Master Computer Academy",
  "marks": "480/500",
  "grade": "A+"
}
```

---

#### Change certificate status

```
PATCH /api/admin/certificates/{id}/status
Authorization: Bearer <token>
```

```json
{ "status": "REVOKED" }
```

Valid values: `ACTIVE`, `REVOKED`, `PENDING`.  
Certificates are **never physically deleted** — use `REVOKED` to invalidate a certificate while keeping the historical record.

---

#### Verification history

```
GET /api/admin/certificates/{id}/verification-history?page=0&size=10
Authorization: Bearer <token>
```

Returns every public verification event for a certificate, newest first.

```json
{
  "success": true,
  "message": "Verification history retrieved",
  "data": {
    "content": [
      { "id": 42, "certificateNumber": "MCA-2024-001", "verifiedAt": "2024-09-20T13:45:00" }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

---

### Error response shape

Every error uses the same envelope:

```json
{
  "success": false,
  "message": "Human-readable description",
  "errors": { "fieldName": "Validation message" },
  "timestamp": "2024-09-20T14:00:00"
}
```

`errors` is only present for validation failures (HTTP 400). Stack traces are **never** returned to clients.

| HTTP Status | Situation |
|---|---|
| `400 Bad Request` | Validation failed / malformed JSON |
| `401 Unauthorized` | Missing or invalid JWT |
| `403 Forbidden` | Valid JWT but insufficient role |
| `404 Not Found` | Resource does not exist |
| `409 Conflict` | Duplicate certificate number |
| `500 Internal Server Error` | Unexpected error (details logged server-side only) |

---

## 8. Admin Authentication

The system uses **stateless JWT authentication** (no sessions, no cookies).

1. Call `POST /api/admin/auth/login` with email and password.
2. Copy the `token` from the response.
3. Include `Authorization: Bearer <token>` on every admin request.
4. Tokens expire after 24 hours by default (`JWT_EXPIRATION_MS`).
5. To get a new token, call `/login` again.

**Password storage:** Passwords are hashed with BCrypt (cost factor 12) before being stored. Plain-text passwords are never persisted or returned in any response.

**Seed admin:** On the first startup of the `dev` profile, a seed admin account is created from `ADMIN_EMAIL` / `ADMIN_PASSWORD` env vars. This mechanism is disabled on `prod`. Change the password immediately after the first login.

---

## 9. Database Structure

### `admin_users`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK AUTO_INCREMENT | |
| `email` | VARCHAR(255) UNIQUE NOT NULL | Indexed |
| `password` | VARCHAR(255) NOT NULL | BCrypt hash |
| `role` | VARCHAR(20) NOT NULL | `ADMIN` |
| `created_at` | DATETIME NOT NULL | JPA Auditing |
| `updated_at` | DATETIME NOT NULL | JPA Auditing |

### `certificates`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK AUTO_INCREMENT | |
| `certificate_number` | VARCHAR(100) UNIQUE NOT NULL | Indexed — primary lookup key |
| `student_name` | VARCHAR(255) NOT NULL | Indexed |
| `student_photo_url` | VARCHAR(1024) | External URL |
| `course_name` | VARCHAR(255) NOT NULL | Indexed |
| `issue_date` | DATE NOT NULL | |
| `duration` | VARCHAR(100) | |
| `institution_name` | VARCHAR(255) | Default: "Master Computer Academy" |
| `marks` | VARCHAR(50) | e.g. "450/500" |
| `grade` | VARCHAR(10) | e.g. "A+" |
| `status` | VARCHAR(20) NOT NULL | `ACTIVE` / `REVOKED` / `PENDING`. Indexed |
| `created_at` | DATETIME NOT NULL | JPA Auditing |
| `updated_at` | DATETIME NOT NULL | JPA Auditing |

### `verification_logs`

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK AUTO_INCREMENT | |
| `certificate_id` | BIGINT NOT NULL FK → `certificates.id` | Indexed |
| `verified_at` | DATETIME NOT NULL | Indexed |

No visitor PII (IP, email, name, phone) is stored — only which certificate was checked and when.

### Entity relationships

```
Certificate  (1) ────── (N)  VerificationLog
```

---

## 10. Testing

### Run all tests

```bash
mvn clean test
```

Tests run against an **H2 in-memory database** (`MODE=MySQL`) — no MySQL instance needed.

### Test results

```
Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
```

### Test coverage breakdown

| Test class | Tests | What is covered |
|---|---|---|
| `JwtUtilTest` | 7 | Token generation, validation, claim extraction, expiry, tamper detection |
| `CertificateServiceTest` | 10 | Create, duplicate, verify (active/revoked/not found), get by ID, update fields, update status, 404 on missing |
| `AuthServiceTest` | 3 | Valid login, unknown email, wrong password |
| `HealthControllerTest` | 1 | Health endpoint returns UP without auth |
| `PublicCertificateControllerTest` | 4 | Active cert, revoked cert, 404, no auth required |
| `AuthControllerTest` | 5 | Valid login, invalid creds, blank email, invalid email format, missing body |
| `AdminCertificateControllerTest` | 9 | 401 without token (×2), create valid/duplicate/missing-fields, list, get/404, status update |

### Run a single test class

```bash
mvn test -Dtest=CertificateServiceTest
```

---

## 11. Production Considerations

### Before going live

- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Generate a strong `JWT_SECRET` (`openssl rand -base64 32`)
- [ ] Use a dedicated MySQL user with only the permissions it needs
- [ ] Set `CORS_ALLOWED_ORIGIN` to your actual frontend domain (e.g. `https://www.mastercomputeracademy.com`)
- [ ] Change the seed admin password and consider disabling the `AdminSeeder` entirely
- [ ] Disable Swagger (`springdoc.swagger-ui.enabled=false` is already set in the prod profile)
- [ ] Use HTTPS (TLS termination at load balancer or reverse proxy level)
- [ ] Review and tune `HikariCP` pool sizes for your expected load

### Schema migrations

The prod profile uses `spring.jpa.hibernate.ddl-auto=validate` — Hibernate will refuse to start if the schema doesn't match the entities. Use **Flyway** to manage schema changes:

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

Place migration scripts in `src/main/resources/db/migration/` as `V1__init.sql`, `V2__...sql`, etc.

### JWT token rotation

- Default expiry is 24 hours. Reduce for higher-security environments.
- Implement a token refresh endpoint if needed.
- Store `JWT_SECRET` in a secrets manager (AWS Secrets Manager, HashiCorp Vault, etc.) — never in source code.

### QR code support

The backend is ready. Certificate numbers are stable unique identifiers.  
Frontend steps to add QR support:
1. Generate a QR image encoding `https://YOUR-DOMAIN/certificate-verification?certificate={certificateNumber}`.
2. Allow download of the QR image from the admin portal.
3. When scanned, the landing page calls `GET /api/certificates/verify/{certificateNumber}`.

No backend changes are required.

### Logging

The prod profile logs at `INFO` level. For production, route logs to a centralised system (ELK, CloudWatch, Grafana Loki, etc.) and set up alerts on `ERROR` and `WARN` entries from `com.mastercomputeracademy`.

---

*Built with Spring Boot 3.2.5 · Java 17 · MySQL 8*
