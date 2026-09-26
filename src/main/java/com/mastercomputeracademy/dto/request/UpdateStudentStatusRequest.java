package com.mastercomputeracademy.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request to update a student's exam form status via
 * PATCH /api/admin/students/{id}/status
 *
 * The frontend sends:
 *   { "examForm": "Exam Form Submitted" }
 *   { "examForm": "Exam Form Pending" }
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Request to change a student's exam form status")
public class UpdateStudentStatusRequest {

    @NotBlank(message = "examForm is required")
    @Size(max = 50, message = "examForm must not exceed 50 characters")
    @Schema(
        description = "Exam form submission status",
        allowableValues = {"Exam Form Submitted", "Exam Form Pending"},
        example = "Exam Form Submitted"
    )
    private String examForm;
}
