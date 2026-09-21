package com.mastercomputeracademy.service;

import com.mastercomputeracademy.dto.response.PagedResponse;
import com.mastercomputeracademy.dto.response.VerificationLogResponse;

public interface VerificationLogService {

    /**
     * Returns paginated verification history for a given certificate (admin use).
     */
    PagedResponse<VerificationLogResponse> getVerificationHistory(Long certificateId, int page, int size);

    /**
     * Returns the total number of times a certificate has been verified.
     */
    long countVerifications(Long certificateId);
}
