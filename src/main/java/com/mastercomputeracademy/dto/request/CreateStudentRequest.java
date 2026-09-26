package com.mastercomputeracademy.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Text fields for creating a new student admission record.
 * Sent as the 'data' part of a multipart/form-data request
 * alongside an optional 'photo' file part.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Fields to create a new student admission record")
public class CreateStudentRequest {

    @NotBlank(message = "Student ID is required")
    @Size(max = 50, message = "Student ID must not exceed 50 characters")
    @Schema(example = "MCA-STU-001")
    private String studentId;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String middleName;

    @NotBlank(message = "Surname is required")
    @Size(max = 100)
    private String surname;

    @Size(max = 255)
    private String applicantName;

    @Size(max = 255)
    private String motherName;

    private LocalDate dateOfBirth;

    @Size(max = 20)
    private String gender;

    @Size(max = 20)
    private String maritalStatus;

    @Size(max = 20)
    private String aadhaarNumber;

    @NotBlank(message = "Mobile number is required")
    @Size(max = 15)
    private String ownMobile;

    @Size(max = 15)
    private String otherMobile;

    @Size(max = 50)
    private String houseNo;

    @Size(max = 255)
    private String street;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String tahsil;

    @Size(max = 100)
    private String district;

    @Size(max = 10)
    private String pinCode;

    @Size(max = 50)
    private String qualification;

    @Size(max = 20)
    private String category;

    @NotBlank(message = "Course is required")
    @Size(max = 255)
    private String course;

    /** Multi-select list of enrolled courses sent by the frontend form. */
    private List<String> courses;

    @NotNull(message = "Admission date is required")
    private LocalDate admissionDate;

    @Size(max = 50)
    private String courseDuration;

    @Size(max = 50)
    private String batchTime;

    private BigDecimal totalFees;
    private BigDecimal feesPaid;

    @Size(max = 50)
    private String receiptNumber;

    private LocalDate receiptDate;

    @Size(max = 2000)
    private String notes;
}
