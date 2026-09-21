package com.mastercomputeracademy.dto.response;

import com.mastercomputeracademy.entity.VerificationLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A single certificate verification event")
public class VerificationLogResponse {

    @Schema(description = "Log entry ID", example = "42")
    private Long id;

    @Schema(description = "Certificate number that was verified", example = "MCA-2024-001")
    private String certificateNumber;

    @Schema(description = "Timestamp when the certificate was verified")
    private LocalDateTime verifiedAt;

    public static VerificationLogResponse fromEntity(VerificationLog log) {
        return VerificationLogResponse.builder()
                .id(log.getId())
                .certificateNumber(log.getCertificate().getCertificateNumber())
                .verifiedAt(log.getVerifiedAt())
                .build();
    }
}
