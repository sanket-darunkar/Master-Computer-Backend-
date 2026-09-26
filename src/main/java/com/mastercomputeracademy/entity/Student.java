package com.mastercomputeracademy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a student enrolled at Master Computer Academy.
 *
 * studentId is a human-readable unique ID assigned by the admin
 * (e.g. MCA-STU-001). It is the primary lookup key exposed to the UI.
 *
 * Passwords, payment credentials, and other sensitive personal details
 * beyond what is needed for admission management are intentionally excluded.
 */
@Entity
@Table(
    name = "students",
    indexes = {
        @Index(name = "idx_student_student_id",   columnList = "student_id",   unique = true),
        @Index(name = "idx_student_own_mobile",   columnList = "own_mobile"),
        @Index(name = "idx_student_first_name",   columnList = "first_name"),
        @Index(name = "idx_student_surname",      columnList = "surname"),
        @Index(name = "idx_student_course",       columnList = "course"),
        @Index(name = "idx_student_status",       columnList = "status"),
        @Index(name = "idx_student_admission_dt", columnList = "admission_date")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Identity ──────────────────────────────────────────────────────────

    @NotBlank
    @Column(name = "student_id", nullable = false, unique = true, length = 50)
    private String studentId;           // e.g. MCA-STU-001

    // ── Personal ──────────────────────────────────────────────────────────

    @NotBlank
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @NotBlank
    @Column(name = "surname", nullable = false, length = 100)
    private String surname;

    /** Name as it appears on official documents. */
    @Column(name = "applicant_name", length = 255)
    private String applicantName;

    @Column(name = "mother_name", length = 255)
    private String motherName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 20)
    private String gender;              // Male | Female | Other

    @Column(name = "marital_status", length = 20)
    private String maritalStatus;       // Single | Married | Divorced | Widowed

    @Column(name = "aadhaar_number", length = 20)
    private String aadhaarNumber;

    // ── Contact ───────────────────────────────────────────────────────────

    @NotBlank
    @Column(name = "own_mobile", nullable = false, length = 15)
    private String ownMobile;

    @Column(name = "other_mobile", length = 15)
    private String otherMobile;

    // ── Address ───────────────────────────────────────────────────────────

    @Column(name = "house_no", length = 50)
    private String houseNo;

    @Column(name = "street", length = 255)
    private String street;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "tahsil", length = 100)
    private String tahsil;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "pin_code", length = 10)
    private String pinCode;

    // ── Academic ──────────────────────────────────────────────────────────

    @Column(name = "qualification", length = 50)
    private String qualification;       // SSC | HSC | Graduate etc.

    @Column(name = "category", length = 20)
    private String category;            // General | OBC | SC | ST | NT | SBC | EWS | Other

    // ── Course / Admission ────────────────────────────────────────────────

    @NotBlank
    @Column(name = "course", nullable = false, length = 255)
    private String course;

    /**
     * All courses the student is enrolled in (multi-select from the UI).
     * Stored as a pipe-delimited string in the DB column courses_list.
     * The legacy single-value {@link #course} field is kept for backward
     * compatibility and is always set to courses.get(0) by the service.
     */
    @Convert(converter = StringListConverter.class)
    @Column(name = "courses_list", length = 2000)
    @Builder.Default
    private List<String> courses = new ArrayList<>();

    @NotNull
    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @Column(name = "course_duration", length = 50)
    private String courseDuration;      // e.g. "3 Months"

    @Column(name = "batch_time", length = 50)
    private String batchTime;           // e.g. "Morning 9–11"

    // ── Fees ──────────────────────────────────────────────────────────────

    @Column(name = "total_fees", precision = 10, scale = 2)
    private BigDecimal totalFees;

    @Column(name = "fees_paid", precision = 10, scale = 2)
    private BigDecimal feesPaid;

    @Column(name = "receipt_number", length = 50)
    private String receiptNumber;

    @Column(name = "receipt_date")
    private LocalDate receiptDate;

    // ── Misc ──────────────────────────────────────────────────────────────

    @Column(name = "notes", length = 2000)
    private String notes;

    // ── Status ────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StudentStatus status = StudentStatus.ACTIVE;

    /**
     * Exam form submission status as a free-text label set by the admin.
     * Values: "Exam Form Submitted" | "Exam Form Pending" (or null = not set).
     * Kept as VARCHAR rather than an enum so new values can be added without a
     * schema migration.
     */
    @Column(name = "exam_form", length = 50)
    @Builder.Default
    private String examForm = "Exam Form Pending";

    // ── Photo ─────────────────────────────────────────────────────────────

    /**
     * Raw bytes of the student passport photo. Max 2 MB enforced at service layer.
     * NOTE: @Lob intentionally NOT used — Hibernate 6 maps @Lob byte[] to PostgreSQL
     * OID which is incompatible with BYTEA. Without @Lob, byte[] maps to BYTEA correctly.
     */
    @Column(name = "photo_data", columnDefinition = "BYTEA")
    private byte[] photoData;

    @Column(name = "photo_mime_type", length = 20)
    private String photoMimeType;

    // ── Audit ─────────────────────────────────────────────────────────────

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Status enum ───────────────────────────────────────────────────────

    public enum StudentStatus {
        ACTIVE,
        INACTIVE,
        COMPLETED,
        DROPPED
    }

    // ── JPA converter ─────────────────────────────────────────────────────

    /**
     * Converts List<String> ↔ pipe-delimited VARCHAR for the courses_list column.
     * e.g. ["DCA", "Tally"] ↔ "DCA|Tally"
     * An empty list is stored as NULL.
     */
    @Converter
    public static class StringListConverter
            implements AttributeConverter<List<String>, String> {

        private static final String DELIM = "|";

        @Override
        public String convertToDatabaseColumn(List<String> list) {
            if (list == null || list.isEmpty()) return null;
            return String.join(DELIM, list);
        }

        @Override
        public List<String> convertToEntityAttribute(String dbValue) {
            if (dbValue == null || dbValue.isBlank()) return new ArrayList<>();
            List<String> result = new ArrayList<>();
            for (String s : dbValue.split("\\|", -1)) {
                if (!s.isBlank()) result.add(s);
            }
            return result;
        }
    }
}
