package com.mastercomputeracademy.dto.request;

import com.mastercomputeracademy.entity.Certificate.CertificateStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to change the status of a certificate")
public class UpdateCertificateStatusRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "New certificate status", example = "REVOKED", allowableValues = {"ACTIVE", "REVOKED", "PENDING"})
    private CertificateStatus status;
}
