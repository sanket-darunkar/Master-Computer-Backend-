package com.mastercomputeracademy.dto.request;

import com.mastercomputeracademy.entity.Student.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Text fields for updating an existing student record.
 * studentId cannot be changed after creation — it is omitted here.
 * The optional status field is included so the full edit form can
 * change status as part of a comprehensive update.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Fields to update a student record")
public class UpdateStudentRequest {

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

    /** Status change included in the full edit form. */
    private StudentStatus status;

    /** Exam form status — updated inline via the edit form. */
    @Size(max = 50)
    private String examForm;
}
