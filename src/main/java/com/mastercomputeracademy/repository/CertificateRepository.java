package com.mastercomputeracademy.repository;

import com.mastercomputeracademy.entity.Certificate;
import com.mastercomputeracademy.entity.Certificate.CertificateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    // ------------------------------------------------------------------
    // Public verification lookup
    // ------------------------------------------------------------------

    Optional<Certificate> findByCertificateNumber(String certificateNumber);

    boolean existsByCertificateNumber(String certificateNumber);

    // ------------------------------------------------------------------
    // Admin paginated search
    // Searches across certificateNumber, studentName, and courseName.
    // All filters are optional – pass null to skip.
    // Uses database-level LIKE queries; never loads the full table.
    // ------------------------------------------------------------------

    @Query("""
            SELECT c FROM Certificate c
            WHERE (:search IS NULL OR
                   LOWER(c.certificateNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(c.studentName)       LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(c.courseName)        LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:status IS NULL OR c.status = :status)
              AND (:course IS NULL OR LOWER(c.courseName) LIKE LOWER(CONCAT('%', :course, '%')))
            """)
    Page<Certificate> searchCertificates(
            @Param("search") String search,
            @Param("status") CertificateStatus status,
            @Param("course") String course,
            Pageable pageable);
}
