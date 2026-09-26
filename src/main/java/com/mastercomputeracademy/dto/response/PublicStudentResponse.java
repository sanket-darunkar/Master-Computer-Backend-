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
import java.util.Map;

/**
 * Student data returned from the public POST /api/students/lookup endpoint.
 *
 * Omits high-sensitivity PII:
 *   – Aadhaar number
 *   – Mobile numbers  (auth factor — never echoed back)
 *   – Full address
 *   – Internal DB id and audit timestamps
 *
 * Fees ARE included — students have a legitimate need to see their own balance.
 *
 * courseExamStatuses – per-course exam status map so the student portal can
 *   display the badge next to each enrolled course.
 *   e.g. {"DCA": "Exam Form Submitted", "Tally ERP 9": "Exam Form Pending"}
 *
 * examForm – legacy overall status kept for backward compat with older consumers.
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

    // ── Fees ──────────────────────────────────────────────────────────
    private BigDecimal totalFees;
    private BigDecimal feesPaid;
    private String receiptNumber;
    private LocalDate receiptDate;

    // ── Exam Form ─────────────────────────────────────────────────────
    /**
     * Per-course exam form statuses.
     * Key = course name, Value = "Exam Form Submitted" | "Exam Form Pending".
     * Displayed next to each enrolled course on the student portal.
     */
    @Schema(description = "Per-course exam form status map")
    private Map<String, String> courseExamStatuses;

    /**
     * Overall / legacy exam form status.
     * "Exam Form Submitted" only when ALL enrolled courses are submitted.
     * Kept for backward compatibility.
     */
    private String examForm;

    // ── Status ────────────────────────────────────────────────────────
    private StudentStatus status;

    // ── Photo ─────────────────────────────────────────────────────────
    /** Ready-to-use data: URI — set directly as img src. */
    private String studentPhotoUrl;
    /** Base64-encoded photo bytes (backward compat). */
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
                .courseExamStatuses(
                        s.getCourseExamStatuses() != null && !s.getCourseExamStatuses().isEmpty()
                                ? s.getCourseExamStatuses()
                                : null)
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
