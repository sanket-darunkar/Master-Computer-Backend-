package com.mastercomputeracademy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mastercomputeracademy.dto.request.UpdateCertificateStatusRequest;
import com.mastercomputeracademy.dto.response.CertificateResponse;
import com.mastercomputeracademy.dto.response.PagedResponse;
import com.mastercomputeracademy.entity.Certificate.CertificateStatus;
import com.mastercomputeracademy.exception.DuplicateCertificateNumberException;
import com.mastercomputeracademy.exception.ResourceNotFoundException;
import com.mastercomputeracademy.service.CertificateService;
import com.mastercomputeracademy.service.VerificationLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AdminCertificateController – integration tests")
class AdminCertificateControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private CertificateService     certificateService;
    @MockBean private VerificationLogService verificationLogService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // ── Minimal valid JSON for the 'data' part ────────────────────────
    private static final String VALID_CREATE_JSON =
        """
        {"certificateNumber":"MCA-2024-001","studentName":"Rahul Sharma",
         "courseName":"Diploma in Computer Application",
         "issueDate":"2024-03-15","duration":"6 Months",
         "institutionName":"Master Computer Academy","marks":"450/500","grade":"A+"}
        """;

    private static final String VALID_UPDATE_JSON =
        """
        {"studentName":"Rahul Sharma","courseName":"Diploma in Computer Application",
         "issueDate":"2024-03-15","duration":"6 Months",
         "institutionName":"Master Computer Academy","marks":"450/500","grade":"A+",
         "removePhoto":false}
        """;

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private CertificateResponse buildAdminResponse(Long id, String number) {
        return CertificateResponse.builder()
                .id(id)
                .certificateNumber(number)
                .studentName("Rahul Sharma")
                .courseName("Diploma in Computer Application")
                .issueDate(LocalDate.of(2024, 3, 15))
                .institutionName("Master Computer Academy")
                .status(CertificateStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ------------------------------------------------------------------
    // Unauthorized (no token) – Task: "Unauthorized admin endpoint"
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/admin/certificates – no token returns 401")
    void getCertificates_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/certificates"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/admin/certificates – no token returns 401")
    void createCertificate_noToken_returns401() throws Exception {
        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE, VALID_CREATE_JSON.getBytes());

        mockMvc.perform(multipart("/api/admin/certificates").file(dataPart))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    // Create certificate – valid multipart request (no photo)
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/admin/certificates – valid multipart (no photo) returns 201")
    void createCertificate_validRequest_noPhoto_returns201() throws Exception {
        when(certificateService.createCertificate(any(), isNull()))
                .thenReturn(buildAdminResponse(1L, "MCA-2024-001"));

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE, VALID_CREATE_JSON.getBytes());

        mockMvc.perform(multipart("/api/admin/certificates").file(dataPart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.certificateNumber").value("MCA-2024-001"));
    }

    // ------------------------------------------------------------------
    // Create certificate – with valid JPEG photo
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/admin/certificates – valid multipart with JPEG photo returns 201")
    void createCertificate_withValidJpegPhoto_returns201() throws Exception {
        CertificateResponse responseWithPhoto = CertificateResponse.builder()
                .id(1L).certificateNumber("MCA-2024-001").studentName("Rahul Sharma")
                .courseName("Diploma in Computer Application")
                .issueDate(LocalDate.of(2024, 3, 15))
                .institutionName("Master Computer Academy")
                .status(CertificateStatus.ACTIVE)
                .photoData("base64encodedphoto")
                .photoMimeType("image/jpeg")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(certificateService.createCertificate(any(), any()))
                .thenReturn(responseWithPhoto);

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE, VALID_CREATE_JSON.getBytes());
        // Minimal valid JPEG magic bytes
        byte[] jpegBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        MockMultipartFile photoPart = new MockMultipartFile(
                "photo", "photo.jpg", "image/jpeg", jpegBytes);

        mockMvc.perform(multipart("/api/admin/certificates").file(dataPart).file(photoPart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.photoData").value("base64encodedphoto"))
                .andExpect(jsonPath("$.data.photoMimeType").value("image/jpeg"));
    }

    // ------------------------------------------------------------------
    // Create certificate – invalid file type
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/admin/certificates – invalid file type returns 400")
    void createCertificate_invalidFileType_returns400() throws Exception {
        when(certificateService.createCertificate(any(), any()))
                .thenThrow(new IllegalArgumentException(
                        "Invalid photo type. Only JPG and PNG images are accepted."));

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE, VALID_CREATE_JSON.getBytes());
        MockMultipartFile photoPart = new MockMultipartFile(
                "photo", "malicious.pdf", "application/pdf", "fake content".getBytes());

        mockMvc.perform(multipart("/api/admin/certificates").file(dataPart).file(photoPart))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(
                        "Invalid photo type. Only JPG and PNG images are accepted."));
    }

    // ------------------------------------------------------------------
    // Create certificate – duplicate certificate number
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/admin/certificates – duplicate number returns 409")
    void createCertificate_duplicateNumber_returns409() throws Exception {
        when(certificateService.createCertificate(any(), any()))
                .thenThrow(new DuplicateCertificateNumberException("MCA-2024-001"));

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE, VALID_CREATE_JSON.getBytes());

        mockMvc.perform(multipart("/api/admin/certificates").file(dataPart))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ------------------------------------------------------------------
    // Create certificate – missing required fields
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/admin/certificates – missing required fields returns 400")
    void createCertificate_missingFields_returns400() throws Exception {
        String missingFields = "{\"studentName\":\"Test\"}";
        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE, missingFields.getBytes());

        mockMvc.perform(multipart("/api/admin/certificates").file(dataPart))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ------------------------------------------------------------------
    // List certificates
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/admin/certificates – returns paginated list")
    void getCertificates_authenticated_returnsList() throws Exception {
        PagedResponse<CertificateResponse> paged = PagedResponse.<CertificateResponse>builder()
                .content(List.of(buildAdminResponse(1L, "MCA-2024-001")))
                .page(0).size(10).totalElements(1).totalPages(1).first(true).last(true)
                .build();

        when(certificateService.getCertificates(anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/admin/certificates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].certificateNumber").value("MCA-2024-001"));
    }

    // ------------------------------------------------------------------
    // Get by ID
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/admin/certificates/{id} – found returns 200")
    void getCertificateById_found_returns200() throws Exception {
        when(certificateService.getCertificateById(1L))
                .thenReturn(buildAdminResponse(1L, "MCA-2024-001"));

        mockMvc.perform(get("/api/admin/certificates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/admin/certificates/{id} – not found returns 404")
    void getCertificateById_notFound_returns404() throws Exception {
        when(certificateService.getCertificateById(99L))
                .thenThrow(new ResourceNotFoundException("Certificate not found with id: 99"));

        mockMvc.perform(get("/api/admin/certificates/99"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // Update certificate – with PNG photo
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/admin/certificates/{id} – update with PNG photo returns 200")
    void updateCertificate_withPhoto_returns200() throws Exception {
        CertificateResponse updated = CertificateResponse.builder()
                .id(1L).certificateNumber("MCA-2024-001").studentName("Rahul Sharma Updated")
                .courseName("Advanced Java").issueDate(LocalDate.of(2024, 6, 1))
                .institutionName("Master Computer Academy")
                .status(CertificateStatus.ACTIVE)
                .photoData("updatedbase64").photoMimeType("image/png")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(certificateService.updateCertificate(eq(1L), any(), any()))
                .thenReturn(updated);

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE, VALID_UPDATE_JSON.getBytes());
        MockMultipartFile photoPart = new MockMultipartFile(
                "photo", "new.png", "image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});

        mockMvc.perform(multipart("/api/admin/certificates/1")
                        .file(dataPart).file(photoPart)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.photoMimeType").value("image/png"));
    }

    // ------------------------------------------------------------------
    // Update certificate – without replacing photo
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/admin/certificates/{id} – update without photo change returns 200")
    void updateCertificate_noPhotoChange_returns200() throws Exception {
        when(certificateService.updateCertificate(eq(1L), any(), isNull()))
                .thenReturn(buildAdminResponse(1L, "MCA-2024-001"));

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE, VALID_UPDATE_JSON.getBytes());

        mockMvc.perform(multipart("/api/admin/certificates/1")
                        .file(dataPart)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ------------------------------------------------------------------
    // Status update (PATCH)
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/admin/certificates/{id}/status – REVOKED returns 200")
    void updateStatus_revoked_returns200() throws Exception {
        CertificateResponse revoked = CertificateResponse.builder()
                .id(1L).certificateNumber("MCA-2024-001").studentName("Rahul Sharma")
                .courseName("Diploma").issueDate(LocalDate.of(2024, 3, 15))
                .institutionName("Master Computer Academy")
                .status(CertificateStatus.REVOKED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(certificateService.updateCertificateStatus(eq(1L), any())).thenReturn(revoked);

        mockMvc.perform(patch("/api/admin/certificates/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateCertificateStatusRequest(CertificateStatus.REVOKED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REVOKED"));
    }
}
