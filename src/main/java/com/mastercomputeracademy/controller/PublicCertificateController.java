package com.mastercomputeracademy.controller;

import com.mastercomputeracademy.dto.response.ApiResponse;
import com.mastercomputeracademy.dto.response.CertificateResponse;
import com.mastercomputeracademy.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoint – no authentication required.
 *
 * Used by students and employers to verify a certificate by its number.
 * Also the target of QR code deep-links:
 *   https://YOUR-DOMAIN/certificate-verification?certificate={certificateNumber}
 * The frontend calls this API after reading the QR parameter.
 *
 * Intentionally exposes only safe public certificate fields.
 * No PII (phone, email, address, payment) is returned.
 */
@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@Tag(name = "Public Certificate Verification", description = "Publicly accessible certificate verification – no authentication required")
public class PublicCertificateController {

    private final CertificateService certificateService;

    @GetMapping("/verify/{certificateNumber}")
    @Operation(
        summary = "Verify a certificate",
        description = """
            Looks up a certificate by its unique certificate number.
            
            - If **ACTIVE**: returns safe public certificate data and records a verification log.
            - If **REVOKED**: returns the certificate with REVOKED status.
            - If **not found**: returns HTTP 404.
            
            This endpoint is intentionally public – no JWT is required.
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Certificate found",
            content = @Content(schema = @Schema(implementation = CertificateResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Certificate not found"),
    })
    public ResponseEntity<ApiResponse<CertificateResponse>> verifyCertificate(
            @PathVariable
            @Parameter(description = "Unique certificate number", example = "MCA-2024-001")
            String certificateNumber) {

        CertificateResponse certificate = certificateService.verifyCertificate(certificateNumber);
        return ResponseEntity.ok(ApiResponse.success("Certificate found", certificate));
    }
}
