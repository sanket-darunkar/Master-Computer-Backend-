package com.mastercomputeracademy.controller;

import com.mastercomputeracademy.dto.response.CertificateResponse;
import com.mastercomputeracademy.entity.Certificate.CertificateStatus;
import com.mastercomputeracademy.exception.ResourceNotFoundException;
import com.mastercomputeracademy.service.CertificateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PublicCertificateController – integration tests")
class PublicCertificateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CertificateService certificateService;

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private CertificateResponse buildPublicResponse(String number, CertificateStatus status) {
        return CertificateResponse.builder()
                .certificateNumber(number)
                .studentName("Rahul Sharma")
                .courseName("Diploma in Computer Application")
                .issueDate(LocalDate.of(2024, 3, 15))
                .duration("6 Months")
                .institutionName("Master Computer Academy")
                .marks("450/500")
                .grade("A+")
                .status(status)
                .build();
    }

    // ------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/certificates/verify/{number} – ACTIVE cert returns 200 with public fields")
    void verify_activeCertificate_returns200() throws Exception {
        when(certificateService.verifyCertificate("MCA-2024-001"))
                .thenReturn(buildPublicResponse("MCA-2024-001", CertificateStatus.ACTIVE));

        mockMvc.perform(get("/api/certificates/verify/MCA-2024-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.certificateNumber").value("MCA-2024-001"))
                .andExpect(jsonPath("$.data.studentName").value("Rahul Sharma"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                // id must NOT be present in the public response
                .andExpect(jsonPath("$.data.id").doesNotExist())
                .andExpect(jsonPath("$.data.createdAt").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/certificates/verify/{number} – REVOKED cert returns 200 with REVOKED status")
    void verify_revokedCertificate_returns200WithRevokedStatus() throws Exception {
        when(certificateService.verifyCertificate("MCA-2024-002"))
                .thenReturn(buildPublicResponse("MCA-2024-002", CertificateStatus.REVOKED));

        mockMvc.perform(get("/api/certificates/verify/MCA-2024-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REVOKED"));
    }

    @Test
    @DisplayName("GET /api/certificates/verify/{number} – not found returns 404")
    void verify_notFound_returns404() throws Exception {
        when(certificateService.verifyCertificate("INVALID-999"))
                .thenThrow(new ResourceNotFoundException("Certificate not found: INVALID-999"));

        mockMvc.perform(get("/api/certificates/verify/INVALID-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Certificate not found: INVALID-999"));
    }

    @Test
    @DisplayName("GET /api/certificates/verify – public endpoint requires NO authentication")
    void verify_noAuthRequired_permitAll() throws Exception {
        // No Authorization header at all – must not get 401
        when(certificateService.verifyCertificate("MCA-2024-001"))
                .thenReturn(buildPublicResponse("MCA-2024-001", CertificateStatus.ACTIVE));

        mockMvc.perform(get("/api/certificates/verify/MCA-2024-001"))
                .andExpect(status().isOk());
    }
}
