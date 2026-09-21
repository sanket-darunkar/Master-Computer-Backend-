package com.mastercomputeracademy.service;

import com.mastercomputeracademy.dto.request.CreateCertificateRequest;
import com.mastercomputeracademy.dto.request.UpdateCertificateRequest;
import com.mastercomputeracademy.dto.request.UpdateCertificateStatusRequest;
import com.mastercomputeracademy.dto.response.CertificateResponse;
import com.mastercomputeracademy.entity.Certificate;
import com.mastercomputeracademy.entity.Certificate.CertificateStatus;
import com.mastercomputeracademy.entity.VerificationLog;
import com.mastercomputeracademy.exception.DuplicateCertificateNumberException;
import com.mastercomputeracademy.exception.ResourceNotFoundException;
import com.mastercomputeracademy.repository.CertificateRepository;
import com.mastercomputeracademy.repository.VerificationLogRepository;
import com.mastercomputeracademy.service.impl.CertificateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CertificateService – unit tests")
class CertificateServiceTest {

    @Mock private CertificateRepository     certificateRepository;
    @Mock private VerificationLogRepository verificationLogRepository;

    @InjectMocks private CertificateServiceImpl certificateService;

    private Certificate activeCertificate;
    private Certificate revokedCertificate;

    @BeforeEach
    void setUp() {
        activeCertificate = Certificate.builder()
                .id(1L)
                .certificateNumber("MCA-2024-001")
                .studentName("Rahul Sharma")
                .courseName("Diploma in Computer Application")
                .issueDate(LocalDate.of(2024, 3, 15))
                .institutionName("Master Computer Academy")
                .status(CertificateStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        revokedCertificate = Certificate.builder()
                .id(2L)
                .certificateNumber("MCA-2024-002")
                .studentName("Priya Patel")
                .courseName("Tally with GST")
                .issueDate(LocalDate.of(2024, 1, 10))
                .institutionName("Master Computer Academy")
                .status(CertificateStatus.REVOKED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ------------------------------------------------------------------
    // verifyCertificate
    // ------------------------------------------------------------------

    @Test
    @DisplayName("verifyCertificate – ACTIVE certificate returns public response and logs verification")
    void verifyCertificate_activeCert_returnsPublicResponseAndLogsVerification() {
        when(certificateRepository.findByCertificateNumber("MCA-2024-001"))
                .thenReturn(Optional.of(activeCertificate));
        when(verificationLogRepository.save(any(VerificationLog.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CertificateResponse response = certificateService.verifyCertificate("MCA-2024-001");

        assertThat(response.getCertificateNumber()).isEqualTo("MCA-2024-001");
        assertThat(response.getStudentName()).isEqualTo("Rahul Sharma");
        assertThat(response.getStatus()).isEqualTo(CertificateStatus.ACTIVE);
        // id and timestamps are NOT included in the public response
        assertThat(response.getId()).isNull();
        assertThat(response.getCreatedAt()).isNull();

        verify(verificationLogRepository, times(1)).save(any(VerificationLog.class));
    }

    @Test
    @DisplayName("verifyCertificate – REVOKED certificate still returns a response and logs the lookup")
    void verifyCertificate_revokedCert_returnsRevokedStatusAndLogs() {
        when(certificateRepository.findByCertificateNumber("MCA-2024-002"))
                .thenReturn(Optional.of(revokedCertificate));
        when(verificationLogRepository.save(any(VerificationLog.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CertificateResponse response = certificateService.verifyCertificate("MCA-2024-002");

        assertThat(response.getStatus()).isEqualTo(CertificateStatus.REVOKED);
        verify(verificationLogRepository, times(1)).save(any(VerificationLog.class));
    }

    @Test
    @DisplayName("verifyCertificate – non-existent number throws ResourceNotFoundException")
    void verifyCertificate_notFound_throwsResourceNotFoundException() {
        when(certificateRepository.findByCertificateNumber("INVALID-999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.verifyCertificate("INVALID-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("INVALID-999");

        verify(verificationLogRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // createCertificate
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createCertificate – valid request persists and returns admin response")
    void createCertificate_validRequest_returnsSavedCertificate() {
        CreateCertificateRequest request = new CreateCertificateRequest(
                "MCA-2024-003",
                "Amit Kumar",
                null,
                "MS Office",
                LocalDate.of(2024, 5, 20),
                "3 Months",
                "Master Computer Academy",
                "95/100",
                "A+");

        when(certificateRepository.existsByCertificateNumber("MCA-2024-003")).thenReturn(false);
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> {
            Certificate c = inv.getArgument(0);
            // Simulate DB assigning id and timestamps
            Certificate saved = Certificate.builder()
                    .id(3L)
                    .certificateNumber(c.getCertificateNumber())
                    .studentName(c.getStudentName())
                    .courseName(c.getCourseName())
                    .issueDate(c.getIssueDate())
                    .duration(c.getDuration())
                    .institutionName(c.getInstitutionName())
                    .marks(c.getMarks())
                    .grade(c.getGrade())
                    .status(CertificateStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return saved;
        });

        CertificateResponse response = certificateService.createCertificate(request);

        assertThat(response.getCertificateNumber()).isEqualTo("MCA-2024-003");
        assertThat(response.getStudentName()).isEqualTo("Amit Kumar");
        assertThat(response.getStatus()).isEqualTo(CertificateStatus.ACTIVE);
        assertThat(response.getId()).isEqualTo(3L);  // admin view includes id
    }

    @Test
    @DisplayName("createCertificate – duplicate certificate number throws DuplicateCertificateNumberException")
    void createCertificate_duplicateNumber_throwsDuplicateCertificateNumberException() {
        CreateCertificateRequest request = new CreateCertificateRequest(
                "MCA-2024-001",
                "Another Student",
                null,
                "Some Course",
                LocalDate.now(),
                null,
                null,
                null,
                null);

        when(certificateRepository.existsByCertificateNumber("MCA-2024-001")).thenReturn(true);

        assertThatThrownBy(() -> certificateService.createCertificate(request))
                .isInstanceOf(DuplicateCertificateNumberException.class)
                .hasMessageContaining("MCA-2024-001");

        verify(certificateRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // getCertificateById
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getCertificateById – existing id returns admin response")
    void getCertificateById_existingId_returnsAdminResponse() {
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(activeCertificate));

        CertificateResponse response = certificateService.getCertificateById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCertificateNumber()).isEqualTo("MCA-2024-001");
    }

    @Test
    @DisplayName("getCertificateById – missing id throws ResourceNotFoundException")
    void getCertificateById_missingId_throwsResourceNotFoundException() {
        when(certificateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.getCertificateById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------
    // updateCertificate
    // ------------------------------------------------------------------

    @Test
    @DisplayName("updateCertificate – updates editable fields correctly")
    void updateCertificate_validRequest_updatesFields() {
        UpdateCertificateRequest request = new UpdateCertificateRequest(
                "Rahul Sharma Updated",
                null,
                "Advanced Java",
                LocalDate.of(2024, 6, 1),
                "6 Months",
                "Master Computer Academy",
                "480/500",
                "A+");

        when(certificateRepository.findById(1L)).thenReturn(Optional.of(activeCertificate));
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> inv.getArgument(0));

        CertificateResponse response = certificateService.updateCertificate(1L, request);

        assertThat(response.getStudentName()).isEqualTo("Rahul Sharma Updated");
        assertThat(response.getCourseName()).isEqualTo("Advanced Java");
        assertThat(response.getMarks()).isEqualTo("480/500");
    }

    // ------------------------------------------------------------------
    // updateCertificateStatus
    // ------------------------------------------------------------------

    @Test
    @DisplayName("updateCertificateStatus – changes status to REVOKED")
    void updateCertificateStatus_toRevoked_statusUpdated() {
        UpdateCertificateStatusRequest request = new UpdateCertificateStatusRequest(CertificateStatus.REVOKED);

        when(certificateRepository.findById(1L)).thenReturn(Optional.of(activeCertificate));
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> inv.getArgument(0));

        CertificateResponse response = certificateService.updateCertificateStatus(1L, request);

        assertThat(response.getStatus()).isEqualTo(CertificateStatus.REVOKED);

        // Verify the entity had its status mutated before save
        ArgumentCaptor<Certificate> captor = ArgumentCaptor.forClass(Certificate.class);
        verify(certificateRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(CertificateStatus.REVOKED);
    }

    @Test
    @DisplayName("updateCertificateStatus – missing certificate throws ResourceNotFoundException")
    void updateCertificateStatus_missingCertificate_throwsResourceNotFoundException() {
        when(certificateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                certificateService.updateCertificateStatus(999L,
                        new UpdateCertificateStatusRequest(CertificateStatus.REVOKED)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
