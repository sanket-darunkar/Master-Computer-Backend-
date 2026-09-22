package com.mastercomputeracademy.service;

import com.mastercomputeracademy.dto.request.CreateCertificateRequest;
import com.mastercomputeracademy.dto.request.UpdateCertificateRequest;
import com.mastercomputeracademy.dto.request.UpdateCertificateStatusRequest;
import com.mastercomputeracademy.dto.response.CertificateResponse;
import com.mastercomputeracademy.dto.response.PagedResponse;
import com.mastercomputeracademy.entity.Certificate.CertificateStatus;
import org.springframework.web.multipart.MultipartFile;

public interface CertificateService {

    // ------------------------------------------------------------------
    // Public
    // ------------------------------------------------------------------

    /** Verifies a certificate by number and records a verification log. */
    CertificateResponse verifyCertificate(String certificateNumber);

    // ------------------------------------------------------------------
    // Admin
    // ------------------------------------------------------------------

    /**
     * Creates a new certificate.
     * @param request   text fields (validated)
     * @param photo     optional photo file; null = no photo
     */
    CertificateResponse createCertificate(CreateCertificateRequest request, MultipartFile photo);

    PagedResponse<CertificateResponse> getCertificates(
            int page, int size, String search, CertificateStatus status, String course);

    CertificateResponse getCertificateById(Long id);

    /**
     * Updates a certificate.
     * @param id        certificate database ID
     * @param request   text fields (validated); request.isRemovePhoto() clears existing photo
     * @param photo     optional new photo file; replaces existing when non-null; null = no change
     */
    CertificateResponse updateCertificate(Long id, UpdateCertificateRequest request, MultipartFile photo);

    CertificateResponse updateCertificateStatus(Long id, UpdateCertificateStatusRequest request);
}
