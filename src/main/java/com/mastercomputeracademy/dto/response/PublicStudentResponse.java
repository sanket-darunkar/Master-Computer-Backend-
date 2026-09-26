package com.mastercomputeracademy.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mastercomputeracademy.entity.Student;
import com.mastercomputeracademy.entity.Student.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.Base64;

/**
 * Student data returned from the public /api/students/lookup endpoint.
 *
 * Intentionally omits PII that has no business being exposed publicly:
 *   – Aadhaar number
 *   – Mobile numbers
 *   – Address (houseNo, street, city, tahsil, district, pinCode)
 *   – Fee details (totalFees, feesPaid, receiptNumber, receiptDate)
 *   – Internal DB id and audit timestamps
 *
 * What IS returned is just enough for a student to confirm their own
 * enrolment details (course, batch, status) and see their photo.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Public-safe student record (PII fields excluded)")
public class PublicStudentResponse {

    @Schema(description = "Human-readable student ID, e.g. MCA-2026-001")
    private String studentId;

    // ── Name ──────────────────────────────────────────────────────────
    private String firstName;
    private String middleName;
    private String surname;
    /** Name as it appears on official documents. */
    private String applicantName;
    private String motherName;

    // ── Basic personal (non-sensitive) ────────────────────────────────
    private LocalDate dateOfBirth;
    private String gender;

    // ── Academic ──────────────────────────────────────────────────────
    private String qualification;
    private String category;

    // ── Course / Admission ────────────────────────────────────────────
    private String course;
    private LocalDate admissionDate;
    private String courseDuration;
    private String batchTime;

    // ── Status ────────────────────────────────────────────────────────
    private StudentStatus status;

    // ── Photo ─────────────────────────────────────────────────────────
    /** Base64-encoded photo. Null when no photo has been uploaded. */
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
                .course(s.getCourse())
                .admissionDate(s.getAdmissionDate())
                .courseDuration(s.getCourseDuration())
                .batchTime(s.getBatchTime())
                .status(s.getStatus());

        if (s.getPhotoData() != null && s.getPhotoData().length > 0) {
            b.photoData(Base64.getEncoder().encodeToString(s.getPhotoData()));
            b.photoMimeType(s.getPhotoMimeType());
        }

        return b.build();
    }
}
