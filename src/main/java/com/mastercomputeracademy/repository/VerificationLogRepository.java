package com.mastercomputeracademy.repository;

import com.mastercomputeracademy.entity.VerificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerificationLogRepository extends JpaRepository<VerificationLog, Long> {

    /**
     * Retrieve all verification events for a specific certificate,
     * newest first, paginated – used by the admin "view verification history" screen.
     */
    Page<VerificationLog> findByCertificateIdOrderByVerifiedAtDesc(Long certificateId, Pageable pageable);

    /**
     * Count total verifications for a certificate – used in admin detail view.
     */
    long countByCertificateId(Long certificateId);

    /**
     * Fetch all logs for a certificate (non-paginated, for small result sets
     * such as admin detail export). Ordered newest-first.
     */
    List<VerificationLog> findByCertificateIdOrderByVerifiedAtDesc(Long certificateId);
}
