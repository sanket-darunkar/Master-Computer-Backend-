package com.mastercomputeracademy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Audit log entry created every time a certificate is publicly verified.
 *
 * Deliberately minimal – we do NOT store visitor IP, name, email, or any
 * personally identifiable information beyond the timestamp and which
 * certificate was looked up.
 */
@Entity
@Table(
    name = "verification_logs",
    indexes = {
        @Index(name = "idx_vlog_certificate_id", columnList = "certificate_id"),
        @Index(name = "idx_vlog_verified_at",    columnList = "verified_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "certificate_id", nullable = false)
    private Certificate certificate;

    @Column(name = "verified_at", nullable = false)
    @Builder.Default
    private LocalDateTime verifiedAt = LocalDateTime.now();
}
