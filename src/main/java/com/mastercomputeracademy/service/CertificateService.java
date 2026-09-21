package com.mastercomputeracademy.service;

import com.mastercomputeracademy.dto.request.CreateCertificateRequest;
import com.mastercomputeracademy.dto.request.UpdateCertificateRequest;
import com.mastercomputeracademy.dto.request.UpdateCertificateStatusRequest;
import com.mastercomputeracademy.dto.response.CertificateResponse;
import com.mastercomputeracademy.dto.response.PagedResponse;
import com.mastercomputeracademy.entity.Certificate.CertificateStatus;

public interface CertificateService {

    // ------------------------------------------------------------------
    // Public
    // ------------------------------------------------------------------

    /** Verifies a certificate by number and records a verification log. */
    CertificateResponse verifyCertificate(String certificateNumber);

    // ------------------------------------------------------------------
    // Admin
    // ------------------------------------------------------------------

    CertificateResponse createCertificate(CreateCertificateRequest request);

    PagedResponse<CertificateResponse> getCertificates(
            int page, int size, String search, CertificateStatus status, String course);

    CertificateResponse getCertificateById(Long id);

    CertificateResponse updateCertificate(Long id, UpdateCertificateRequest request);

    CertificateResponse updateCertificateStatus(Long id, UpdateCertificateStatusRequest request);
}
