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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateServiceImpl implements CertificateService {

    // Max upload size = 2 MB
    private static final long   MAX_PHOTO_BYTES    = 2 * 1024 * 1024L;
    private static final Set<String> ALLOWED_MIME  = Set.of("image/jpeg", "image/png");
    private static final Set<String> ALLOWED_EXT   = Set.of("jpg", "jpeg", "png");

    private final CertificateRepository     certificateRepository;
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

        VerificationLog logEntry = VerificationLog.builder()
                .certificate(certificate)
                .verifiedAt(LocalDateTime.now())
                .build();
        verificationLogRepository.save(logEntry);

        log.info("Certificate verified: {} | status: {}", certificateNumber, certificate.getStatus());
        return CertificateResponse.fromEntityForPublic(certificate);
    }

    // ------------------------------------------------------------------
    // Admin
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public CertificateResponse createCertificate(CreateCertificateRequest request, MultipartFile photo) {
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
                .courseName(request.getCourseName())
                .issueDate(request.getIssueDate())
                .duration(request.getDuration())
                .institutionName(institutionName)
                .marks(request.getMarks())
                .grade(request.getGrade())
                .status(CertificateStatus.ACTIVE)
                .build();

        applyPhoto(certificate, photo);

        Certificate saved = certificateRepository.save(certificate);
        log.info("Certificate created: id={}, number={}", saved.getId(), saved.getCertificateNumber());
        return CertificateResponse.fromEntityForAdmin(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CertificateResponse> getCertificates(
            int page, int size, String search, CertificateStatus status, String course) {

        String searchParam = (search != null && !search.isBlank()) ? search.trim() : "";
        String courseParam = (course != null && !course.isBlank()) ? course.trim() : "";

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Certificate> resultPage = certificateRepository
                .searchCertificates(searchParam, status, courseParam, pageable);

        return PagedResponse.from(resultPage.map(CertificateResponse::fromEntityForAdmin));
    }

    @Override
    @Transactional(readOnly = true)
    public CertificateResponse getCertificateById(Long id) {
        return CertificateResponse.fromEntityForAdmin(findCertificateById(id));
    }

    @Override
    @Transactional
    public CertificateResponse updateCertificate(Long id, UpdateCertificateRequest request, MultipartFile photo) {
        log.debug("Admin updating certificate id={}", id);

        Certificate certificate = findCertificateById(id);

        String institutionName = (request.getInstitutionName() != null && !request.getInstitutionName().isBlank())
                ? request.getInstitutionName()
                : "Master Computer Academy";

        certificate.setStudentName(request.getStudentName());
        certificate.setCourseName(request.getCourseName());
        certificate.setIssueDate(request.getIssueDate());
        certificate.setDuration(request.getDuration());
        certificate.setInstitutionName(institutionName);
        certificate.setMarks(request.getMarks());
        certificate.setGrade(request.getGrade());

        if (photo != null && !photo.isEmpty()) {
            // New photo supplied — validate and store it
            applyPhoto(certificate, photo);
        } else if (request.isRemovePhoto()) {
            // Explicit removal requested — clear all photo fields
            certificate.setPhotoData(null);
            certificate.setPhotoMimeType(null);
            certificate.setStudentPhotoUrl(null);
        }
        // else: no change to photo — leave existing data intact

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
    // Photo validation + storage
    // ------------------------------------------------------------------

    /**
     * Validates and stores a photo on the certificate entity.
     * Checks:
     *   - File is not empty
     *   - Size <= 2 MB
     *   - MIME type is image/jpeg or image/png (from Content-Type header)
     *   - File extension is jpg/jpeg/png (from original filename — secondary check)
     *   - Does NOT trust the original filename for storage (uses byte array only)
     *
     * Throws IllegalArgumentException on validation failure.
     * The message is safe to surface to the client.
     */
    private void applyPhoto(Certificate certificate, MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            return; // no photo — leave existing data unchanged
        }

        // Size check
        if (photo.getSize() > MAX_PHOTO_BYTES) {
            throw new IllegalArgumentException(
                "Photo file is too large. Maximum allowed size is 2 MB.");
        }

        // MIME type check (Content-Type header)
        String contentType = photo.getContentType();
        if (contentType == null || !ALLOWED_MIME.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                "Invalid photo type. Only JPG and PNG images are accepted.");
        }

        // Extension check (defence-in-depth — do NOT trust as sole validator)
        String originalFilename = photo.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1)
                                         .toLowerCase();
            // Path traversal guard: extension must be purely alphanumeric
            if (!ext.matches("[a-z0-9]+") || !ALLOWED_EXT.contains(ext)) {
                throw new IllegalArgumentException(
                    "Invalid photo file extension. Only .jpg, .jpeg, and .png are accepted.");
            }
        }

        try {
            byte[] bytes = photo.getBytes();
            certificate.setPhotoData(bytes);
            certificate.setPhotoMimeType(contentType.toLowerCase());
            // Clear legacy URL field — photo is now stored in the DB
            certificate.setStudentPhotoUrl(null);
            log.debug("Photo stored: {} bytes, type={}", bytes.length, contentType);
        } catch (IOException e) {
            log.error("Failed to read uploaded photo bytes", e);
            throw new IllegalArgumentException("Failed to read photo file. Please try again.");
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Certificate findCertificateById(Long id) {
        return certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));
    }
}
