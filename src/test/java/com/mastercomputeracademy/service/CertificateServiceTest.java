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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

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

    // ── helpers ───────────────────────────────────────────────────────

    private CreateCertificateRequest createRequest(String certNumber) {
        return new CreateCertificateRequest(
                certNumber, "Amit Kumar", "MS Office",
                LocalDate.of(2024, 5, 20), "3 Months",
                "Master Computer Academy", "95/100", "A+");
    }

    private UpdateCertificateRequest updateRequest() {
        return new UpdateCertificateRequest(
                "Rahul Sharma Updated", "Advanced Java",
                LocalDate.of(2024, 6, 1), "6 Months",
                "Master Computer Academy", "480/500", "A+", false);
    }

    private Certificate savedCert(CreateCertificateRequest req, Long id) {
        return Certificate.builder()
                .id(id)
                .certificateNumber(req.getCertificateNumber())
                .studentName(req.getStudentName())
                .courseName(req.getCourseName())
                .issueDate(req.getIssueDate())
                .duration(req.getDuration())
                .institutionName(req.getInstitutionName())
                .marks(req.getMarks())
                .grade(req.getGrade())
                .status(CertificateStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ------------------------------------------------------------------
    // verifyCertificate
    // ------------------------------------------------------------------

    @Test
    @DisplayName("verifyCertificate – ACTIVE returns public response and logs")
    void verifyCertificate_active_returnsPublicResponseAndLogs() {
        when(certificateRepository.findByCertificateNumber("MCA-2024-001"))
                .thenReturn(Optional.of(activeCertificate));
        when(verificationLogRepository.save(any(VerificationLog.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CertificateResponse response = certificateService.verifyCertificate("MCA-2024-001");

        assertThat(response.getCertificateNumber()).isEqualTo("MCA-2024-001");
        assertThat(response.getStatus()).isEqualTo(CertificateStatus.ACTIVE);
        assertThat(response.getId()).isNull();        // public view: no id
        assertThat(response.getCreatedAt()).isNull(); // public view: no timestamps
        verify(verificationLogRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("verifyCertificate – REVOKED still logs and returns REVOKED status")
    void verifyCertificate_revoked_returnsRevokedAndLogs() {
        when(certificateRepository.findByCertificateNumber("MCA-2024-002"))
                .thenReturn(Optional.of(revokedCertificate));
        when(verificationLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CertificateResponse response = certificateService.verifyCertificate("MCA-2024-002");

        assertThat(response.getStatus()).isEqualTo(CertificateStatus.REVOKED);
        verify(verificationLogRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("verifyCertificate – not found throws ResourceNotFoundException")
    void verifyCertificate_notFound_throws() {
        when(certificateRepository.findByCertificateNumber("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.verifyCertificate("INVALID"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(verificationLogRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // verifyCertificate – photo in response
    // ------------------------------------------------------------------

    @Test
    @DisplayName("verifyCertificate – certificate with photo returns base64 photoData")
    void verifyCertificate_withPhoto_returnsBase64Photo() {
        byte[] fakeBytes = new byte[]{1, 2, 3};
        activeCertificate.setPhotoData(fakeBytes);
        activeCertificate.setPhotoMimeType("image/jpeg");

        when(certificateRepository.findByCertificateNumber("MCA-2024-001"))
                .thenReturn(Optional.of(activeCertificate));
        when(verificationLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CertificateResponse response = certificateService.verifyCertificate("MCA-2024-001");

        assertThat(response.getPhotoData()).isNotNull();
        assertThat(response.getPhotoMimeType()).isEqualTo("image/jpeg");
        assertThat(response.getStudentPhotoUrl()).isNull(); // binary photo takes priority
    }

    @Test
    @DisplayName("verifyCertificate – certificate without photo returns null photo fields")
    void verifyCertificate_noPhoto_returnsNullPhotoFields() {
        when(certificateRepository.findByCertificateNumber("MCA-2024-001"))
                .thenReturn(Optional.of(activeCertificate));
        when(verificationLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CertificateResponse response = certificateService.verifyCertificate("MCA-2024-001");

        assertThat(response.getPhotoData()).isNull();
        assertThat(response.getPhotoMimeType()).isNull();
    }

    // ------------------------------------------------------------------
    // createCertificate – without photo
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createCertificate – no photo saves certificate with null photoData")
    void createCertificate_noPhoto_savesNullPhotoData() {
        CreateCertificateRequest req = createRequest("MCA-2024-003");
        when(certificateRepository.existsByCertificateNumber("MCA-2024-003")).thenReturn(false);
        when(certificateRepository.save(any())).thenAnswer(inv -> {
            Certificate c = inv.getArgument(0);
            c = savedCert(req, 3L);
            return c;
        });

        CertificateResponse response = certificateService.createCertificate(req, null);

        assertThat(response.getCertificateNumber()).isEqualTo("MCA-2024-003");
        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getPhotoData()).isNull();
    }

    // ------------------------------------------------------------------
    // createCertificate – with valid JPEG
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createCertificate – valid JPEG photo is stored and returned as base64")
    void createCertificate_withValidJpeg_storesPhoto() {
        CreateCertificateRequest req = createRequest("MCA-2024-004");
        byte[] jpegBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

        MultipartFile photo = new MockMultipartFile(
                "photo", "student.jpg", "image/jpeg", jpegBytes);

        when(certificateRepository.existsByCertificateNumber("MCA-2024-004")).thenReturn(false);
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> {
            Certificate c = inv.getArgument(0);
            c.setId(4L);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return c;
        });

        CertificateResponse response = certificateService.createCertificate(req, photo);

        assertThat(response.getPhotoData()).isNotNull();
        assertThat(response.getPhotoMimeType()).isEqualTo("image/jpeg");

        // Verify bytes were stored on the entity
        ArgumentCaptor<Certificate> captor = ArgumentCaptor.forClass(Certificate.class);
        verify(certificateRepository).save(captor.capture());
        assertThat(captor.getValue().getPhotoData()).isEqualTo(jpegBytes);
        assertThat(captor.getValue().getPhotoMimeType()).isEqualTo("image/jpeg");
    }

    // ------------------------------------------------------------------
    // createCertificate – invalid file type
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createCertificate – invalid MIME type throws IllegalArgumentException")
    void createCertificate_invalidMimeType_throwsIllegalArgument() {
        CreateCertificateRequest req = createRequest("MCA-2024-005");
        MultipartFile photo = new MockMultipartFile(
                "photo", "doc.pdf", "application/pdf", "fake pdf".getBytes());

        when(certificateRepository.existsByCertificateNumber("MCA-2024-005")).thenReturn(false);

        assertThatThrownBy(() -> certificateService.createCertificate(req, photo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only JPG and PNG");

        verify(certificateRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // createCertificate – file too large
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createCertificate – file larger than 2 MB throws IllegalArgumentException")
    void createCertificate_fileTooLarge_throwsIllegalArgument() {
        CreateCertificateRequest req = createRequest("MCA-2024-006");
        // 2 MB + 1 byte
        byte[] tooLarge = new byte[2 * 1024 * 1024 + 1];
        MultipartFile photo = new MockMultipartFile(
                "photo", "big.jpg", "image/jpeg", tooLarge);

        when(certificateRepository.existsByCertificateNumber("MCA-2024-006")).thenReturn(false);

        assertThatThrownBy(() -> certificateService.createCertificate(req, photo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("too large");

        verify(certificateRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // createCertificate – duplicate number
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createCertificate – duplicate number throws DuplicateCertificateNumberException")
    void createCertificate_duplicate_throws() {
        when(certificateRepository.existsByCertificateNumber("MCA-2024-001")).thenReturn(true);

        assertThatThrownBy(() -> certificateService.createCertificate(createRequest("MCA-2024-001"), null))
                .isInstanceOf(DuplicateCertificateNumberException.class);
        verify(certificateRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // getCertificateById
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getCertificateById – found returns admin response with id")
    void getCertificateById_found_returnsAdminResponse() {
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(activeCertificate));

        CertificateResponse response = certificateService.getCertificateById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCertificateNumber()).isEqualTo("MCA-2024-001");
    }

    @Test
    @DisplayName("getCertificateById – not found throws ResourceNotFoundException")
    void getCertificateById_notFound_throws() {
        when(certificateRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> certificateService.getCertificateById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------
    // updateCertificate – without photo (no change)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("updateCertificate – no photo supplied leaves existing photo intact")
    void updateCertificate_noPhoto_existingPhotoUnchanged() {
        byte[] existing = new byte[]{1, 2, 3};
        activeCertificate.setPhotoData(existing);
        activeCertificate.setPhotoMimeType("image/png");

        when(certificateRepository.findById(1L)).thenReturn(Optional.of(activeCertificate));
        when(certificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        certificateService.updateCertificate(1L, updateRequest(), null);

        ArgumentCaptor<Certificate> captor = ArgumentCaptor.forClass(Certificate.class);
        verify(certificateRepository).save(captor.capture());
        // Existing photo bytes must be untouched
        assertThat(captor.getValue().getPhotoData()).isEqualTo(existing);
        assertThat(captor.getValue().getPhotoMimeType()).isEqualTo("image/png");
    }

    // ------------------------------------------------------------------
    // updateCertificate – with new PNG photo
    // ------------------------------------------------------------------

    @Test
    @DisplayName("updateCertificate – new PNG photo replaces existing")
    void updateCertificate_withNewPhoto_replacesExisting() {
        activeCertificate.setPhotoData(new byte[]{9, 9, 9});
        activeCertificate.setPhotoMimeType("image/jpeg");

        byte[] newBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
        MultipartFile newPhoto = new MockMultipartFile(
                "photo", "new.png", "image/png", newBytes);

        when(certificateRepository.findById(1L)).thenReturn(Optional.of(activeCertificate));
        when(certificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        certificateService.updateCertificate(1L, updateRequest(), newPhoto);

        ArgumentCaptor<Certificate> captor = ArgumentCaptor.forClass(Certificate.class);
        verify(certificateRepository).save(captor.capture());
        assertThat(captor.getValue().getPhotoData()).isEqualTo(newBytes);
        assertThat(captor.getValue().getPhotoMimeType()).isEqualTo("image/png");
    }

    // ------------------------------------------------------------------
    // updateCertificateStatus
    // ------------------------------------------------------------------

    @Test
    @DisplayName("updateCertificateStatus – status changed to REVOKED")
    void updateCertificateStatus_toRevoked_statusUpdated() {
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(activeCertificate));
        when(certificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CertificateResponse response = certificateService.updateCertificateStatus(
                1L, new UpdateCertificateStatusRequest(CertificateStatus.REVOKED));

        assertThat(response.getStatus()).isEqualTo(CertificateStatus.REVOKED);
        ArgumentCaptor<Certificate> captor = ArgumentCaptor.forClass(Certificate.class);
        verify(certificateRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(CertificateStatus.REVOKED);
    }

    @Test
    @DisplayName("updateCertificateStatus – not found throws ResourceNotFoundException")
    void updateCertificateStatus_notFound_throws() {
        when(certificateRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> certificateService.updateCertificateStatus(
                999L, new UpdateCertificateStatusRequest(CertificateStatus.REVOKED)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
