package com.mastercomputeracademy.controller;

import com.mastercomputeracademy.dto.request.CreateCertificateRequest;
import com.mastercomputeracademy.dto.request.UpdateCertificateRequest;
import com.mastercomputeracademy.dto.request.UpdateCertificateStatusRequest;
import com.mastercomputeracademy.dto.response.ApiResponse;
import com.mastercomputeracademy.dto.response.CertificateResponse;
import com.mastercomputeracademy.dto.response.PagedResponse;
import com.mastercomputeracademy.dto.response.VerificationLogResponse;
import com.mastercomputeracademy.entity.Certificate.CertificateStatus;
import com.mastercomputeracademy.service.CertificateService;
import com.mastercomputeracademy.service.VerificationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only certificate management endpoints.
 * All routes require a valid JWT with ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/certificates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin – Certificates", description = "Admin certificate management (JWT required)")
@SecurityRequirement(name = "bearerAuth")
public class AdminCertificateController {

    private final CertificateService certificateService;
    private final VerificationLogService verificationLogService;

    // ------------------------------------------------------------------
    // Create
    // ------------------------------------------------------------------

    @PostMapping
    @Operation(summary = "Create certificate", description = "Creates a new certificate. Certificate number must be unique.")
    public ResponseEntity<ApiResponse<CertificateResponse>> createCertificate(
            @Valid @RequestBody CreateCertificateRequest request) {

        CertificateResponse created = certificateService.createCertificate(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Certificate created successfully", created));
    }

    // ------------------------------------------------------------------
    // List / Search
    // ------------------------------------------------------------------

    @GetMapping
    @Operation(
        summary = "List certificates",
        description = "Returns a paginated list of certificates with optional filtering by search term, status, and course name. "
                    + "Uses database-level queries – never loads the full table into memory."
    )
    public ResponseEntity<ApiResponse<PagedResponse<CertificateResponse>>> getCertificates(
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Page number (0-indexed)", example = "0") int page,

            @RequestParam(defaultValue = "10")
            @Parameter(description = "Page size (max 100)", example = "10") int size,

            @RequestParam(required = false)
            @Parameter(description = "Search term – matches certificate number, student name, or course name") String search,

            @RequestParam(required = false)
            @Parameter(description = "Filter by status", example = "ACTIVE") CertificateStatus status,

            @RequestParam(required = false)
            @Parameter(description = "Filter by course name (partial match)") String course) {

        // Guard against absurdly large page sizes
        int safeSize = Math.min(size, 100);

        PagedResponse<CertificateResponse> result =
                certificateService.getCertificates(page, safeSize, search, status, course);
        return ResponseEntity.ok(ApiResponse.success("Certificates retrieved", result));
    }

    // ------------------------------------------------------------------
    // Get by ID
    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(summary = "Get certificate by ID")
    public ResponseEntity<ApiResponse<CertificateResponse>> getCertificateById(
            @PathVariable @Parameter(description = "Certificate database ID") Long id) {

        CertificateResponse certificate = certificateService.getCertificateById(id);
        return ResponseEntity.ok(ApiResponse.success("Certificate retrieved", certificate));
    }

    // ------------------------------------------------------------------
    // Update
    // ------------------------------------------------------------------

    @PutMapping("/{id}")
    @Operation(summary = "Update certificate", description = "Updates all editable fields of a certificate. Certificate number cannot be changed.")
    public ResponseEntity<ApiResponse<CertificateResponse>> updateCertificate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCertificateRequest request) {

        CertificateResponse updated = certificateService.updateCertificate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Certificate updated successfully", updated));
    }

    // ------------------------------------------------------------------
    // Status change (PATCH)
    // ------------------------------------------------------------------

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Change certificate status",
        description = "Changes the status of a certificate to ACTIVE, REVOKED, or PENDING. "
                    + "Certificates are NOT physically deleted – use REVOKED instead."
    )
    public ResponseEntity<ApiResponse<CertificateResponse>> updateCertificateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCertificateStatusRequest request) {

        CertificateResponse updated = certificateService.updateCertificateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Certificate status updated successfully", updated));
    }

    // ------------------------------------------------------------------
    // Verification history
    // ------------------------------------------------------------------

    @GetMapping("/{id}/verification-history")
    @Operation(
        summary = "Get verification history",
        description = "Returns a paginated list of all public verification events for a certificate."
    )
    public ResponseEntity<ApiResponse<PagedResponse<VerificationLogResponse>>> getVerificationHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<VerificationLogResponse> history =
                verificationLogService.getVerificationHistory(id, page, size);
        return ResponseEntity.ok(ApiResponse.success("Verification history retrieved", history));
    }
}
