package com.mastercomputeracademy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mastercomputeracademy.dto.request.CreateCertificateRequest;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    // ------------------------------------------------------------------
    // Helpers
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
    // Unauthorized access (no credentials at all) – Task: "Unauthorized admin endpoint"
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
        mockMvc.perform(post("/api/admin/certificates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    // Create certificate (authenticated via @WithMockUser)
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/admin/certificates – valid request returns 201")
    void createCertificate_validRequest_returns201() throws Exception {
        CreateCertificateRequest request = new CreateCertificateRequest(
                "MCA-2024-001", "Rahul Sharma", null,
                "Diploma in Computer Application",
                LocalDate.of(2024, 3, 15),
                "6 Months", "Master Computer Academy", "450/500", "A+");

        when(certificateService.createCertificate(any()))
                .thenReturn(buildAdminResponse(1L, "MCA-2024-001"));

        mockMvc.perform(post("/api/admin/certificates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.certificateNumber").value("MCA-2024-001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/admin/certificates – duplicate number returns 409")
    void createCertificate_duplicateNumber_returns409() throws Exception {
        CreateCertificateRequest request = new CreateCertificateRequest(
                "MCA-2024-001", "Other Student", null,
                "MS Office", LocalDate.now(),
                null, null, null, null);

        when(certificateService.createCertificate(any()))
                .thenThrow(new DuplicateCertificateNumberException("MCA-2024-001"));

        mockMvc.perform(post("/api/admin/certificates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/admin/certificates – missing required fields returns 400")
    void createCertificate_missingFields_returns400() throws Exception {
        // certificateNumber, studentName, courseName, issueDate are all required
        String bodyMissingFields = "{\"studentName\":\"Test\"}";

        mockMvc.perform(post("/api/admin/certificates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyMissingFields))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").exists());
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
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].certificateNumber").value("MCA-2024-001"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    // ------------------------------------------------------------------
    // Get by ID
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/admin/certificates/{id} – existing id returns 200")
    void getCertificateById_exists_returns200() throws Exception {
        when(certificateService.getCertificateById(1L))
                .thenReturn(buildAdminResponse(1L, "MCA-2024-001"));

        mockMvc.perform(get("/api/admin/certificates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/admin/certificates/{id} – missing id returns 404")
    void getCertificateById_notFound_returns404() throws Exception {
        when(certificateService.getCertificateById(99L))
                .thenThrow(new ResourceNotFoundException("Certificate not found with id: 99"));

        mockMvc.perform(get("/api/admin/certificates/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ------------------------------------------------------------------
    // Status update (PATCH)
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/admin/certificates/{id}/status – REVOKED returns 200")
    void updateStatus_validStatus_returns200() throws Exception {
        CertificateResponse revokedResponse = CertificateResponse.builder()
                .id(1L).certificateNumber("MCA-2024-001").studentName("Rahul Sharma")
                .courseName("Diploma in Computer Application")
                .issueDate(LocalDate.of(2024, 3, 15))
                .institutionName("Master Computer Academy")
                .status(CertificateStatus.REVOKED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(certificateService.updateCertificateStatus(eq(1L), any()))
                .thenReturn(revokedResponse);

        mockMvc.perform(patch("/api/admin/certificates/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateCertificateStatusRequest(CertificateStatus.REVOKED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REVOKED"));
    }
}
