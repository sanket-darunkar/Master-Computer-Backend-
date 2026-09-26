package com.mastercomputeracademy.controller;

import com.mastercomputeracademy.dto.response.ApiResponse;
import com.mastercomputeracademy.dto.response.PublicStudentResponse;
import com.mastercomputeracademy.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public student portal endpoint – no authentication required.
 *
 * Students can look up their own enrolment record by providing their
 * Student ID and the mobile number registered at the academy.
 * The mobile acts as a lightweight second factor so a student cannot
 * view another student's record just by guessing an ID.
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
@Validated
@Tag(
    name = "Public Student Portal",
    description = "Publicly accessible student record lookup – no authentication required"
)
public class PublicStudentController {

    private final StudentService studentService;

    /**
     * GET /api/students/lookup?studentId=MCA-2026-001&mobile=9403339998
     *
     * Returns HTTP 200 + PII-safe student data on success.
     * Returns HTTP 404 if the studentId is not found.
     * Returns HTTP 401 if the mobile does not match the registered number.
     */
    @GetMapping("/lookup")
    @Operation(
        summary = "Student self-service lookup",
        description = """
            Allows a student to retrieve their own enrolment record without logging in.

            **Required query parameters:**
            - `studentId` – the unique student ID printed on their receipt/form (e.g. `MCA-2026-001`)
            - `mobile`    – the mobile number registered with the academy

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
            @RequestParam
            @NotBlank(message = "studentId is required")
            @Parameter(description = "Student ID as printed on receipt / admission form", example = "MCA-2026-001")
            String studentId,

            @RequestParam
            @NotBlank(message = "mobile is required")
            @Parameter(description = "Mobile number registered with the academy", example = "9403339998")
            String mobile) {

        PublicStudentResponse result = studentService.lookupStudentByStudentId(studentId, mobile);
        return ResponseEntity.ok(ApiResponse.success("Student record found", result));
    }
}
