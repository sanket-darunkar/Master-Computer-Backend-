package com.mastercomputeracademy.controller;

import com.mastercomputeracademy.dto.request.CreateStudentRequest;
import com.mastercomputeracademy.dto.request.UpdateStudentRequest;
import com.mastercomputeracademy.dto.request.UpdateStudentStatusRequest;
import com.mastercomputeracademy.dto.response.ApiResponse;
import com.mastercomputeracademy.dto.response.PagedResponse;
import com.mastercomputeracademy.dto.response.StudentResponse;
import com.mastercomputeracademy.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Admin-only student management endpoints.
 * All routes require a valid JWT with ROLE_ADMIN.
 *
 * Create and Update accept multipart/form-data so that an optional
 * student passport photo can be uploaded alongside the text fields.
 *
 * Multipart parts:
 *   data  – JSON object matching Create/UpdateStudentRequest
 *   photo – optional image file (JPG/PNG, max 2 MB)
 */
@RestController
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin – Students", description = "Admin student management (JWT required)")
@SecurityRequirement(name = "bearerAuth")
public class AdminStudentController {

    private final StudentService studentService;

    // ------------------------------------------------------------------
    // Create
    // ------------------------------------------------------------------

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Create student",
        description = """
            Creates a new student admission record.

            Send as **multipart/form-data** with:
            - `data` part: JSON fields (studentId, firstName, surname, course, admissionDate, ...)
            - `photo` part (optional): passport-style JPG/PNG, max **2 MB**

            Returns HTTP 409 if the studentId already exists.
            """
    )
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            @RequestPart("data") @Valid CreateStudentRequest request,
            @RequestPart(value = "photo", required = false)
            @Parameter(description = "Optional student passport photo (JPG/PNG, max 2 MB)")
            MultipartFile photo) {

        StudentResponse created = studentService.createStudent(request, photo);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student created successfully", created));
    }

    // ------------------------------------------------------------------
    // List / Search
    // ------------------------------------------------------------------

    @GetMapping
    @Operation(
        summary = "List students",
        description = "Returns paginated students. Optionally filter by search term, examForm status, or course."
    )
    public ResponseEntity<ApiResponse<PagedResponse<StudentResponse>>> getStudents(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false)
            @Parameter(description = "Search across student ID, first name, surname, mobile") String search,
            @RequestParam(required = false)
            @Parameter(description = "Exam Form Submitted | Exam Form Pending") String status,
            @RequestParam(required = false)
            @Parameter(description = "Course name (partial match)") String course) {

        int safeSize = Math.min(size, 100);
        PagedResponse<StudentResponse> result =
                studentService.getStudents(page, safeSize, search, status, course);
        return ResponseEntity.ok(ApiResponse.success("Students retrieved", result));
    }

    // ------------------------------------------------------------------
    // Get by ID
    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(summary = "Get student by ID")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Student retrieved", studentService.getStudentById(id)));
    }

    // ------------------------------------------------------------------
    // Update
    // ------------------------------------------------------------------

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Update student",
        description = """
            Updates a student record. studentId cannot be changed after creation.

            Send as **multipart/form-data** with:
            - `data` part: JSON fields (firstName, surname, course, status, ...)
            - `photo` part (optional): replacement passport photo (JPG/PNG, max 2 MB)

            Omit the `photo` part to leave the existing photo unchanged.
            """
    )
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @PathVariable Long id,
            @RequestPart("data") @Valid UpdateStudentRequest request,
            @RequestPart(value = "photo", required = false)
            @Parameter(description = "Optional replacement photo (JPG/PNG, max 2 MB)")
            MultipartFile photo) {

        StudentResponse updated = studentService.updateStudent(id, request, photo);
        return ResponseEntity.ok(ApiResponse.success("Student updated successfully", updated));
    }

    // ------------------------------------------------------------------
    // Status change
    // ------------------------------------------------------------------

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Change student status",
        description = "Changes status to ACTIVE, INACTIVE, COMPLETED, or DROPPED."
    )
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentStatusRequest request) {

        StudentResponse updated = studentService.updateStudentStatus(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Student status updated successfully", updated));
    }
}
