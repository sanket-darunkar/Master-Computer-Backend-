package com.mastercomputeracademy.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request body for PATCH /api/admin/students/{id}/status
 *
 * Two usage modes:
 *
 *   1. Per-course update (preferred):
 *      { "courseName": "DCA", "examForm": "Exam Form Submitted" }
 *      → updates only the DCA entry in courseExamStatuses;
 *        other courses are unchanged.
 *        legacy examForm is re-derived (Submitted only when ALL are Submitted).
 *
 *   2. Global update (backward compat, courseName omitted or blank):
 *      { "examForm": "Exam Form Submitted" }
 *      → sets ALL enrolled courses to the given status
 *        and updates the legacy examForm field.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Request to update exam form status — per-course or global")
public class UpdateStudentStatusRequest {

    @Size(max = 255, message = "courseName must not exceed 255 characters")
    @Schema(
        description = "Course name to update. Omit to apply the status to ALL courses.",
        example = "DCA",
        nullable = true
    )
    private String courseName;

    @NotBlank(message = "examForm is required")
    @Size(max = 50, message = "examForm must not exceed 50 characters")
    @Schema(
        description = "Exam form status to set",
        allowableValues = {"Exam Form Submitted", "Exam Form Pending"},
        example = "Exam Form Submitted"
    )
    private String examForm;
}
