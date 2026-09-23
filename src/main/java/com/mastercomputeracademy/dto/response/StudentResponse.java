package com.mastercomputeracademy.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mastercomputeracademy.entity.Student;
import com.mastercomputeracademy.entity.Student.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Student response DTO.
 *
 * Photo is returned as:
 *   photoData     – Base64-encoded bytes
 *   photoMimeType – e.g. "image/jpeg"
 *
 * The frontend constructs the img src as:
 *   `data:${photoMimeType};base64,${photoData}`
 *
 * Fields intentionally excluded: raw photoData bytes (converted here),
 * any internal DB constraints.
 */
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Student record returned to admin callers")
public class StudentResponse {

    private Long id;
    private String studentId;

    private String firstName;
    private String middleName;
    private String surname;
    private String applicantName;
    private String motherName;
    private LocalDate dateOfBirth;
    private String gender;
    private String maritalStatus;
    private String aadhaarNumber;

    private String ownMobile;
    private String otherMobile;

    private String houseNo;
    private String street;
    private String city;
    private String tahsil;
    private String district;
    private String pinCode;

    private String qualification;
    private String category;

    private String course;
    private LocalDate admissionDate;
    private String courseDuration;
    private String batchTime;

    private BigDecimal totalFees;
    private BigDecimal feesPaid;
    private String receiptNumber;
    private LocalDate receiptDate;

    private String notes;
    private StudentStatus status;

    /** Base64-encoded photo bytes. Null when no photo has been uploaded. */
    private String photoData;
    /** MIME type of the uploaded photo, e.g. "image/jpeg". */
    private String photoMimeType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ------------------------------------------------------------------
    // Factory
    // ------------------------------------------------------------------

    public static StudentResponse fromEntity(Student s) {
        StudentResponseBuilder b = StudentResponse.builder()
                .id(s.getId())
                .studentId(s.getStudentId())
                .firstName(s.getFirstName())
                .middleName(s.getMiddleName())
                .surname(s.getSurname())
                .applicantName(s.getApplicantName())
                .motherName(s.getMotherName())
                .dateOfBirth(s.getDateOfBirth())
                .gender(s.getGender())
                .maritalStatus(s.getMaritalStatus())
                .aadhaarNumber(s.getAadhaarNumber())
                .ownMobile(s.getOwnMobile())
                .otherMobile(s.getOtherMobile())
                .houseNo(s.getHouseNo())
                .street(s.getStreet())
                .city(s.getCity())
                .tahsil(s.getTahsil())
                .district(s.getDistrict())
                .pinCode(s.getPinCode())
                .qualification(s.getQualification())
                .category(s.getCategory())
                .course(s.getCourse())
                .admissionDate(s.getAdmissionDate())
                .courseDuration(s.getCourseDuration())
                .batchTime(s.getBatchTime())
                .totalFees(s.getTotalFees())
                .feesPaid(s.getFeesPaid())
                .receiptNumber(s.getReceiptNumber())
                .receiptDate(s.getReceiptDate())
                .notes(s.getNotes())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt());

        if (s.getPhotoData() != null && s.getPhotoData().length > 0) {
            b.photoData(Base64.getEncoder().encodeToString(s.getPhotoData()));
            b.photoMimeType(s.getPhotoMimeType());
        }

        return b.build();
    }
}
