package com.mastercomputeracademy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a certificate issued by Master Computer Academy.
 *
 * certificateNumber is globally unique and is the primary lookup key
 * for public verification.
 */
@Entity
@Table(
    name = "certificates",
    indexes = {
        @Index(name = "idx_cert_number",       columnList = "certificate_number", unique = true),
        @Index(name = "idx_cert_student_name", columnList = "student_name"),
        @Index(name = "idx_cert_course_name",  columnList = "course_name"),
        @Index(name = "idx_cert_status",       columnList = "status")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "certificate_number", nullable = false, unique = true, length = 100)
    private String certificateNumber;

    @NotBlank
    @Column(name = "student_name", nullable = false, length = 255)
    private String studentName;

    /** URL pointing to the student photo (stored externally, e.g. cloud storage). */
    @Column(name = "student_photo_url", length = 1024)
    private String studentPhotoUrl;

    /**
     * Raw binary bytes of the uploaded student photo.
     * Stored as BYTEA in PostgreSQL.
     * @Lob is intentionally NOT used — Hibernate 6 maps @Lob byte[] to PostgreSQL
     * OID (Large Object), which is incompatible with BYTEA columns.
     * Without @Lob, byte[] maps correctly to BYTEA.
     * Null when no photo has been uploaded.
     * Max 2 MB enforced at the service layer.
     */
    @Column(name = "photo_data", columnDefinition = "BYTEA")
    private byte[] photoData;

    /**
     * MIME type of the uploaded photo, e.g. "image/jpeg" or "image/png".
     * Always set when photoData is non-null.
     */
    @Column(name = "photo_mime_type", length = 20)
    private String photoMimeType;

    @NotBlank
    @Column(name = "course_name", nullable = false, length = 255)
    private String courseName;

    @NotNull
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    /** Human-readable duration string, e.g. "6 Months", "1 Year". */
    @Column(name = "duration", length = 100)
    private String duration;

    @Column(name = "institution_name", length = 255)
    @Builder.Default
    private String institutionName = "Master Computer Academy";

    /** Marks obtained, e.g. "450/500". Stored as string for flexibility. */
    @Column(name = "marks", length = 50)
    private String marks;

    /** Grade awarded, e.g. "A+", "Distinction". */
    @Column(name = "grade", length = 10)
    private String grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CertificateStatus status = CertificateStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------

    @OneToMany(mappedBy = "certificate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VerificationLog> verificationLogs = new ArrayList<>();

    // ------------------------------------------------------------------
    // Status enum
    // ------------------------------------------------------------------

    public enum CertificateStatus {
        ACTIVE,
        REVOKED,
        PENDING
    }
}
