package com.mastercomputeracademy.service.impl;

import com.mastercomputeracademy.dto.request.CreateCertificateRequest;
import com.mastercomputeracademy.dto.request.UpdateCertificateRequest;
import com.mastercomputeracademy.dto.request.UpdateCertificateStatusRequest;
import com.mastercomputeracademy.dto.response.CertificateResponse;
import com.mastercomputeracademy.dto.response.PagedResponse;
import com.mastercomputeracademy.entity.Certificate;
import com.mastercomputeracademy.entity.Certificate.CertificateStatus;
import com.mastercomputeracademy.entity.VerificationLog;
import com.mastercomputeracademy.exception.DuplicateCertificateNumberException;
import com.mastercomputeracademy.exception.ResourceNotFoundException;
import com.mastercomputeracademy.repository.CertificateRepository;
import com.mastercomputeracademy.repository.VerificationLogRepository;
import com.mastercomputeracademy.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final VerificationLogRepository verificationLogRepository;

    // ------------------------------------------------------------------
    // Public
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public CertificateResponse verifyCertificate(String certificateNumber) {
        log.debug("Public verification request for certificate: {}", certificateNumber);

        Certificate certificate = certificateRepository
                .findByCertificateNumber(certificateNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certificate not found: " + certificateNumber));

        // Record the verification regardless of status
        VerificationLog logEntry = VerificationLog.builder()
                .certificate(certificate)
                .verifiedAt(LocalDateTime.now())
                .build();
        verificationLogRepository.save(logEntry);

        log.info("Certificate verified: {} | status: {}", certificateNumber, certificate.getStatus());

        // Return public-safe view (no id, no timestamps)
        return CertificateResponse.fromEntityForPublic(certificate);
    }

    // ------------------------------------------------------------------
    // Admin
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public CertificateResponse createCertificate(CreateCertificateRequest request) {
        log.debug("Admin creating certificate: {}", request.getCertificateNumber());

        if (certificateRepository.existsByCertificateNumber(request.getCertificateNumber())) {
            throw new DuplicateCertificateNumberException(request.getCertificateNumber());
        }

        String institutionName = (request.getInstitutionName() != null && !request.getInstitutionName().isBlank())
                ? request.getInstitutionName()
                : "Master Computer Academy";

        Certificate certificate = Certificate.builder()
                .certificateNumber(request.getCertificateNumber())
                .studentName(request.getStudentName())
                .studentPhotoUrl(request.getStudentPhotoUrl())
                .courseName(request.getCourseName())
                .issueDate(request.getIssueDate())
                .duration(request.getDuration())
                .institutionName(institutionName)
                .marks(request.getMarks())
                .grade(request.getGrade())
                .status(CertificateStatus.ACTIVE)
                .build();

        Certificate saved = certificateRepository.save(certificate);
        log.info("Certificate created: id={}, number={}", saved.getId(), saved.getCertificateNumber());

        return CertificateResponse.fromEntityForAdmin(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CertificateResponse> getCertificates(
            int page, int size, String search, CertificateStatus status, String course) {

        // Normalise null/blank to empty string — the query uses :search = '' to mean "no filter".
        // Never pass null for string params on PostgreSQL: it causes lower(bytea) type errors.
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : "";
        String courseParam = (course != null && !course.isBlank()) ? course.trim() : "";

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Certificate> resultPage = certificateRepository
                .searchCertificates(searchParam, status, courseParam, pageable);

        Page<CertificateResponse> responsePage = resultPage
                .map(CertificateResponse::fromEntityForAdmin);

        return PagedResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public CertificateResponse getCertificateById(Long id) {
        Certificate certificate = findCertificateById(id);
        return CertificateResponse.fromEntityForAdmin(certificate);
    }

    @Override
    @Transactional
    public CertificateResponse updateCertificate(Long id, UpdateCertificateRequest request) {
        log.debug("Admin updating certificate id={}", id);

        Certificate certificate = findCertificateById(id);

        String institutionName = (request.getInstitutionName() != null && !request.getInstitutionName().isBlank())
                ? request.getInstitutionName()
                : "Master Computer Academy";

        certificate.setStudentName(request.getStudentName());
        certificate.setStudentPhotoUrl(request.getStudentPhotoUrl());
        certificate.setCourseName(request.getCourseName());
        certificate.setIssueDate(request.getIssueDate());
        certificate.setDuration(request.getDuration());
        certificate.setInstitutionName(institutionName);
        certificate.setMarks(request.getMarks());
        certificate.setGrade(request.getGrade());

        Certificate saved = certificateRepository.save(certificate);
        log.info("Certificate updated: id={}", saved.getId());

        return CertificateResponse.fromEntityForAdmin(saved);
    }

    @Override
    @Transactional
    public CertificateResponse updateCertificateStatus(Long id, UpdateCertificateStatusRequest request) {
        log.debug("Admin updating status of certificate id={} to {}", id, request.getStatus());

        Certificate certificate = findCertificateById(id);
        certificate.setStatus(request.getStatus());

        Certificate saved = certificateRepository.save(certificate);
        log.info("Certificate status updated: id={}, status={}", saved.getId(), saved.getStatus());

        return CertificateResponse.fromEntityForAdmin(saved);
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private Certificate findCertificateById(Long id) {
        return certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));
    }
}
