package com.mastercomputeracademy.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mastercomputeracademy.entity.Certificate;
import com.mastercomputeracademy.entity.Certificate.CertificateStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Safe certificate response DTO.
 *
 * Photo strategy:
 *   - photoData     – Base64-encoded bytes of the uploaded photo (data URL prefix
 *                     NOT included; the frontend constructs it with photoMimeType).
 *                     Null when no uploaded photo exists.
 *   - photoMimeType – e.g. "image/jpeg", "image/png". Null when photoData is null.
 *   - studentPhotoUrl – legacy URL field kept for backward compatibility.
 *                       Null when the certificate was created with a file upload.
 *
 * The public factory method exposes photoData so the verification page can
 * display the photo. The admin factory method additionally includes id and timestamps.
 *
 * Fields intentionally EXCLUDED from both views:
 *   - raw photoData bytes at the entity level (converted to Base64 String here)
 *   - database internals
 *   - admin passwords / JWT secrets
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Certificate details returned to callers")
public class CertificateResponse {

    @Schema(description = "Internal ID (admin only)", example = "1")
    private Long id;

    @Schema(description = "Unique certificate number", example = "MCA-2024-001")
    private String certificateNumber;

    @Schema(description = "Full name of the student", example = "Rahul Sharma")
    private String studentName;

    /**
     * Legacy URL-based photo. Retained for backward compatibility with
     * certificates that were created before file-upload support was added.
     * Null when the certificate uses the new photoData approach.
     */
    @Schema(description = "Legacy URL of the student photo (null when photoData is present)")
    private String studentPhotoUrl;

    /**
     * Base64-encoded bytes of the uploaded student photo.
     * Use together with photoMimeType to construct a data URL:
     *   src={`data:${photoMimeType};base64,${photoData}`}
     * Null when no photo has been uploaded via file upload.
     */
    @Schema(description = "Base64-encoded student photo bytes (null if no uploaded photo)")
    private String photoData;

    /** MIME type of the uploaded photo, e.g. "image/jpeg". */
    @Schema(description = "MIME type of the uploaded photo", example = "image/jpeg")
    private String photoMimeType;

    @Schema(description = "Name of the course", example = "Diploma in Computer Application")
    private String courseName;

    @Schema(description = "Date the certificate was issued", example = "2024-03-15")
    private LocalDate issueDate;

    @Schema(description = "Duration of the course", example = "6 Months")
    private String duration;

    @Schema(description = "Issuing institution", example = "Master Computer Academy")
    private String institutionName;

    @Schema(description = "Marks obtained", example = "450/500")
    private String marks;

    @Schema(description = "Grade awarded", example = "A+")
    private String grade;

    @Schema(description = "Certificate status", example = "ACTIVE")
    private CertificateStatus status;

    @Schema(description = "Creation timestamp (admin only)")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp (admin only)")
    private LocalDateTime updatedAt;

    // ------------------------------------------------------------------
    // Static factory methods
    // ------------------------------------------------------------------

    /**
     * Public-safe view: exposes only what is needed for certificate verification.
     * Includes photo (as base64) so the verification page can display it.
     * Does NOT include id or timestamps.
     */
    public static CertificateResponse fromEntityForPublic(Certificate cert) {
        CertificateResponseBuilder b = CertificateResponse.builder()
                .certificateNumber(cert.getCertificateNumber())
                .studentName(cert.getStudentName())
                .courseName(cert.getCourseName())
                .issueDate(cert.getIssueDate())
                .duration(cert.getDuration())
                .institutionName(cert.getInstitutionName())
                .marks(cert.getMarks())
                .grade(cert.getGrade())
                .status(cert.getStatus());

        applyPhoto(b, cert);
        return b.build();
    }

    /**
     * Admin view: includes id and audit timestamps.
     */
    public static CertificateResponse fromEntityForAdmin(Certificate cert) {
        CertificateResponseBuilder b = CertificateResponse.builder()
                .id(cert.getId())
                .certificateNumber(cert.getCertificateNumber())
                .studentName(cert.getStudentName())
                .courseName(cert.getCourseName())
                .issueDate(cert.getIssueDate())
                .duration(cert.getDuration())
                .institutionName(cert.getInstitutionName())
                .marks(cert.getMarks())
                .grade(cert.getGrade())
                .status(cert.getStatus())
                .createdAt(cert.getCreatedAt())
                .updatedAt(cert.getUpdatedAt());

        applyPhoto(b, cert);
        return b.build();
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    /**
     * Applies photo fields to the builder.
     * Priority: uploaded binary data > legacy URL.
     * If photoData bytes are present, they are Base64-encoded and placed in
     * the photoData field alongside photoMimeType. The legacy studentPhotoUrl
     * field is only populated when no binary photo exists.
     */
    private static void applyPhoto(CertificateResponseBuilder b, Certificate cert) {
        if (cert.getPhotoData() != null && cert.getPhotoData().length > 0) {
            b.photoData(Base64.getEncoder().encodeToString(cert.getPhotoData()));
            b.photoMimeType(cert.getPhotoMimeType());
        } else if (cert.getStudentPhotoUrl() != null && !cert.getStudentPhotoUrl().isBlank()) {
            b.studentPhotoUrl(cert.getStudentPhotoUrl());
        }
        // else: both null — no photo fields set; @JsonInclude(NON_NULL) omits them
    }
}
