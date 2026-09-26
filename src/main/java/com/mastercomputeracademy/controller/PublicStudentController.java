package com.mastercomputeracademy.controller;

import com.mastercomputeracademy.dto.response.ApiResponse;
import com.mastercomputeracademy.dto.response.PublicStudentResponse;
import com.mastercomputeracademy.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public student portal endpoint – no authentication required.
 *
 * Students look up their own enrolment record by providing their
 * Student ID and the mobile number registered at the academy.
 * The mobile acts as a lightweight second factor.
 *
 * PII deliberately excluded from the response:
 *   – Aadhaar number
 *   – Mobile numbers
 *   – Full address
 *   – Fee details
 */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(
    name = "Public Student Portal",
    description = "Publicly accessible student record lookup – no authentication required"
)
public class PublicStudentController {

    private final StudentService studentService;

    // ------------------------------------------------------------------
    // Request DTO (inline — small and self-contained)
    // ------------------------------------------------------------------

    @Getter @Setter @NoArgsConstructor
    @Schema(description = "Student self-service lookup request")
    public static class LookupRequest {

        @NotBlank(message = "studentId is required")
        @Schema(description = "Student ID as printed on receipt / admission form",
                example = "MCA-2026-001")
        private String studentId;

        @NotBlank(message = "mobile is required")
        @Schema(description = "Mobile number registered with the academy",
                example = "9403339998")
        private String mobile;
    }

    // ------------------------------------------------------------------
    // POST /api/students/lookup
    // ------------------------------------------------------------------

    /**
     * POST /api/students/lookup
     * Body: { "studentId": "MCA-2026-001", "mobile": "9403339998" }
     *
     * Returns HTTP 200 + PII-safe student data on success.
     * Returns HTTP 404 if the studentId is not found.
     * Returns HTTP 401 if the mobile does not match the registered number.
     */
    @PostMapping("/lookup")
    @Operation(
        summary = "Student self-service lookup",
        description = """
            Allows a student to retrieve their own enrolment record without logging in.

            **Request body (JSON):**
            ```json
            { "studentId": "MCA-2026-001", "mobile": "9403339998" }
            ```

            The mobile acts as a second factor: if it does not match either the primary
            or alternate number on file the request is rejected with **HTTP 401**.

            **Sensitive fields intentionally excluded from the response:**
            Aadhaar number, mobile numbers, full address, and fee details.

            This endpoint is **publicly accessible** – no JWT token is required.
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Student record found",
            content = @Content(schema = @Schema(implementation = PublicStudentResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Student ID and mobile number do not match"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "No student found with the supplied Student ID"
        )
    })
    public ResponseEntity<ApiResponse<PublicStudentResponse>> lookupStudent(
            @Valid @RequestBody LookupRequest request) {

        PublicStudentResponse result =
                studentService.lookupStudentByStudentId(request.getStudentId(), request.getMobile());
        return ResponseEntity.ok(ApiResponse.success("Student record found", result));
    }
}
