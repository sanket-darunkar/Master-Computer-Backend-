package com.mastercomputeracademy.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mastercomputeracademy.entity.Student;
import com.mastercomputeracademy.entity.Student.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

/**
 * Student data returned from the public POST /api/students/lookup endpoint.
 *
 * Intentionally omits high-sensitivity PII:
 *   – Aadhaar number
 *   – Mobile numbers  (used as the auth factor — never echo it back)
 *   – Full address
 *   – Internal DB id and audit timestamps
 *
 * Fees ARE included — a student has a legitimate need to see their own
 * outstanding balance. They authenticated with their own mobile, so this
 * is their own data.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Public-safe student record returned after successful self-service lookup")
public class PublicStudentResponse {

    @Schema(description = "Human-readable student ID, e.g. MCA-2026-001")
    private String studentId;

    // ── Name ──────────────────────────────────────────────────────────
    private String firstName;
    private String middleName;
    private String surname;
    private String applicantName;
    private String motherName;

    // ── Basic personal (non-sensitive) ────────────────────────────────
    private LocalDate dateOfBirth;
    private String gender;

    // ── Academic ──────────────────────────────────────────────────────
    private String qualification;
    private String category;

    // ── Course / Admission ────────────────────────────────────────────
    /** Multi-select list of enrolled courses. */
    private List<String> courses;
    /** First enrolled course — backward compatibility. */
    private String course;
    private LocalDate admissionDate;
    private String courseDuration;
    private String batchTime;

    // ── Fees (student's own data — safe to return after mobile auth) ──
    private BigDecimal totalFees;
    private BigDecimal feesPaid;
    private String receiptNumber;
    private LocalDate receiptDate;

    // ── Exam Form ─────────────────────────────────────────────────────
    /** "Exam Form Submitted" | "Exam Form Pending" */
    private String examForm;

    // ── Status ────────────────────────────────────────────────────────
    private StudentStatus status;

    // ── Photo ─────────────────────────────────────────────────────────
    /** Ready-to-use data: URI — set directly as img src. */
    private String studentPhotoUrl;
    /** Base64-encoded photo bytes (kept for backward compat). */
    private String photoData;
    /** MIME type of the uploaded photo, e.g. "image/jpeg". */
    private String photoMimeType;

    // ------------------------------------------------------------------
    // Factory
    // ------------------------------------------------------------------

    public static PublicStudentResponse fromEntity(Student s) {
        PublicStudentResponseBuilder b = PublicStudentResponse.builder()
                .studentId(s.getStudentId())
                .firstName(s.getFirstName())
                .middleName(s.getMiddleName())
                .surname(s.getSurname())
                .applicantName(s.getApplicantName())
                .motherName(s.getMotherName())
                .dateOfBirth(s.getDateOfBirth())
                .gender(s.getGender())
                .qualification(s.getQualification())
                .category(s.getCategory())
                .courses(s.getCourses())
                .course(s.getCourse())
                .admissionDate(s.getAdmissionDate())
                .courseDuration(s.getCourseDuration())
                .batchTime(s.getBatchTime())
                .totalFees(s.getTotalFees())
                .feesPaid(s.getFeesPaid())
                .receiptNumber(s.getReceiptNumber())
                .receiptDate(s.getReceiptDate())
                .examForm(s.getExamForm())
                .status(s.getStatus());

        if (s.getPhotoData() != null && s.getPhotoData().length > 0) {
            String base64 = Base64.getEncoder().encodeToString(s.getPhotoData());
            String mime   = s.getPhotoMimeType();
            b.photoData(base64);
            b.photoMimeType(mime);
            b.studentPhotoUrl("data:" + mime + ";base64," + base64);
        }

        return b.build();
    }
}
