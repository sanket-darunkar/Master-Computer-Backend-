package com.mastercomputeracademy.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to update an existing certificate")
public class UpdateCertificateRequest {

    @NotBlank(message = "Student name is required")
    @Size(max = 255, message = "Student name must not exceed 255 characters")
    @Schema(description = "Full name of the student", example = "Rahul Sharma")
    private String studentName;

    @Size(max = 1024, message = "Photo URL must not exceed 1024 characters")
    @Schema(description = "URL of the student's photo (optional)")
    private String studentPhotoUrl;

    @NotBlank(message = "Course name is required")
    @Size(max = 255, message = "Course name must not exceed 255 characters")
    @Schema(description = "Name of the course", example = "Diploma in Computer Application")
    private String courseName;

    @NotNull(message = "Issue date is required")
    @Schema(description = "Date the certificate was issued", example = "2024-03-15")
    private LocalDate issueDate;

    @Size(max = 100, message = "Duration must not exceed 100 characters")
    @Schema(description = "Duration of the course", example = "6 Months")
    private String duration;

    @Size(max = 255, message = "Institution name must not exceed 255 characters")
    @Schema(description = "Issuing institution name", example = "Master Computer Academy")
    private String institutionName;

    @Size(max = 50, message = "Marks must not exceed 50 characters")
    @Schema(description = "Marks obtained", example = "450/500")
    private String marks;

    @Size(max = 10, message = "Grade must not exceed 10 characters")
    @Schema(description = "Grade awarded", example = "A+")
    private String grade;
}
