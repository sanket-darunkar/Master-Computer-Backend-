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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Admin-only certificate management endpoints.
 * All routes require a valid JWT with ROLE_ADMIN.
 *
 * Create and Update endpoints now accept multipart/form-data
 * so that an optional student photo file can be uploaded alongside
 * the certificate text fields.
 *
 * Multipart parts:
 *   data  – JSON object matching CreateCertificateRequest / UpdateCertificateRequest
 *   photo – optional image file (JPG/PNG, max 2 MB)
 */
@RestController
@RequestMapping("/api/admin/certificates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin – Certificates", description = "Admin certificate management (JWT required)")
@SecurityRequirement(name = "bearerAuth")
public class AdminCertificateController {

    private final CertificateService     certificateService;
    private final VerificationLogService verificationLogService;

    // ------------------------------------------------------------------
    // Create  (multipart/form-data)
    // ------------------------------------------------------------------

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Create certificate",
        description = """
            Creates a new certificate. Certificate number must be unique.
            
            Send as **multipart/form-data** with:
            - `data` part: JSON fields (certificateNumber, studentName, courseName, issueDate, ...)
            - `photo` part (optional): JPG or PNG image, max **2 MB**
            """
    )
    public ResponseEntity<ApiResponse<CertificateResponse>> createCertificate(
            @RequestPart("data")
            @Valid CreateCertificateRequest request,

            @RequestPart(value = "photo", required = false)
            @Parameter(description = "Optional student photo (JPG/PNG, max 2 MB)")
            MultipartFile photo) {

        CertificateResponse created = certificateService.createCertificate(request, photo);
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
        description = "Returns a paginated list of certificates with optional filtering. "
                    + "Uses database-level queries – never loads the full table into memory."
    )
    public ResponseEntity<ApiResponse<PagedResponse<CertificateResponse>>> getCertificates(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CertificateStatus status,
            @RequestParam(required = false) String course) {

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
    public ResponseEntity<ApiResponse<CertificateResponse>> getCertificateById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Certificate retrieved",
                certificateService.getCertificateById(id)));
    }

    // ------------------------------------------------------------------
    // Update  (multipart/form-data)
    // ------------------------------------------------------------------

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Update certificate",
        description = """
            Updates a certificate. Certificate number cannot be changed.
            
            Send as **multipart/form-data** with:
            - `data` part: JSON fields (studentName, courseName, issueDate, ..., removePhoto)
            - `photo` part (optional): new JPG/PNG image to replace the existing one
            
            Set `removePhoto: true` in the `data` part (and omit the `photo` part) to remove
            the existing photo without uploading a replacement.
            """
    )
    public ResponseEntity<ApiResponse<CertificateResponse>> updateCertificate(
            @PathVariable Long id,

            @RequestPart("data")
            @Valid UpdateCertificateRequest request,

            @RequestPart(value = "photo", required = false)
            @Parameter(description = "Optional replacement photo (JPG/PNG, max 2 MB)")
            MultipartFile photo) {

        CertificateResponse updated = certificateService.updateCertificate(id, request, photo);
        return ResponseEntity.ok(ApiResponse.success("Certificate updated successfully", updated));
    }

    // ------------------------------------------------------------------
    // Status change (PATCH)
    // ------------------------------------------------------------------

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Change certificate status",
        description = "Changes status to ACTIVE, REVOKED, or PENDING. Certificates are never physically deleted."
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
    @Operation(summary = "Get verification history")
    public ResponseEntity<ApiResponse<PagedResponse<VerificationLogResponse>>> getVerificationHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<VerificationLogResponse> history =
                verificationLogService.getVerificationHistory(id, page, size);
        return ResponseEntity.ok(ApiResponse.success("Verification history retrieved", history));
    }
}
