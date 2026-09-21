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

/**
 * Safe public-facing certificate response.
 *
 * Fields intentionally EXCLUDED:
 *   - database id (not exposed to the public)
 *   - student phone / email / address
 *   - payment information
 *   - admin data
 *   - any internal fields
 *
 * The admin variant includes id and timestamps via the static factory
 * {@link #fromEntityForAdmin(Certificate)}.
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

    @Schema(description = "URL of the student photo", example = "https://storage.example.com/photos/rahul.jpg")
    private String studentPhotoUrl;

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
     * Does NOT include id or timestamps.
     */
    public static CertificateResponse fromEntityForPublic(Certificate cert) {
        return CertificateResponse.builder()
                .certificateNumber(cert.getCertificateNumber())
                .studentName(cert.getStudentName())
                .studentPhotoUrl(cert.getStudentPhotoUrl())
                .courseName(cert.getCourseName())
                .issueDate(cert.getIssueDate())
                .duration(cert.getDuration())
                .institutionName(cert.getInstitutionName())
                .marks(cert.getMarks())
                .grade(cert.getGrade())
                .status(cert.getStatus())
                .build();
    }

    /**
     * Admin view: includes id and audit timestamps.
     */
    public static CertificateResponse fromEntityForAdmin(Certificate cert) {
        return CertificateResponse.builder()
                .id(cert.getId())
                .certificateNumber(cert.getCertificateNumber())
                .studentName(cert.getStudentName())
                .studentPhotoUrl(cert.getStudentPhotoUrl())
                .courseName(cert.getCourseName())
                .issueDate(cert.getIssueDate())
                .duration(cert.getDuration())
                .institutionName(cert.getInstitutionName())
                .marks(cert.getMarks())
                .grade(cert.getGrade())
                .status(cert.getStatus())
                .createdAt(cert.getCreatedAt())
                .updatedAt(cert.getUpdatedAt())
                .build();
    }
}
