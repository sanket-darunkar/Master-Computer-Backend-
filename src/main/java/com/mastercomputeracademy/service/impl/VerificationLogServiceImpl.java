package com.mastercomputeracademy.service.impl;

import com.mastercomputeracademy.dto.response.PagedResponse;
import com.mastercomputeracademy.dto.response.VerificationLogResponse;
import com.mastercomputeracademy.exception.ResourceNotFoundException;
import com.mastercomputeracademy.repository.CertificateRepository;
import com.mastercomputeracademy.repository.VerificationLogRepository;
import com.mastercomputeracademy.service.VerificationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationLogServiceImpl implements VerificationLogService {

    private final VerificationLogRepository verificationLogRepository;
    private final CertificateRepository certificateRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VerificationLogResponse> getVerificationHistory(Long certificateId, int page, int size) {
        // Validate the certificate exists first
        if (!certificateRepository.existsById(certificateId)) {
            throw new ResourceNotFoundException("Certificate not found with id: " + certificateId);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<VerificationLogResponse> responsePage = verificationLogRepository
                .findByCertificateIdOrderByVerifiedAtDesc(certificateId, pageable)
                .map(VerificationLogResponse::fromEntity);

        return PagedResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public long countVerifications(Long certificateId) {
        return verificationLogRepository.countByCertificateId(certificateId);
    }
}
