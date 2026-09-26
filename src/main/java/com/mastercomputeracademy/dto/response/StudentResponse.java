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
import java.util.List;

/**
 * Student response DTO.
 *
 * Photo is returned as both:
 *   studentPhotoUrl – a ready-to-use data: URI (used directly by the frontend)
 *   photoData       – Base64-encoded bytes  (kept for backward compat)
 *   photoMimeType   – e.g. "image/jpeg"
 *
 * courses  – full multi-select list (e.g. ["DCA", "Tally"])
 * course   – first element of courses (backward compat with older code)
 * examForm – free-text exam form status ("Exam Form Submitted" | "Exam Form Pending")
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

    /** Multi-select list of enrolled courses. */
    private List<String> courses;
    /** First enrolled course — backward compatibility with older consumers. */
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

    /**
     * Exam form submission status.
     * Values: "Exam Form Submitted" | "Exam Form Pending"
     */
    private String examForm;

    /**
     * Ready-to-use data: URI for the student photo.
     * Format: "data:{mimeType};base64,{bytes}"
     * Null when no photo has been uploaded.
     * Used directly as an img src by the frontend.
     */
    private String studentPhotoUrl;

    /** Base64-encoded photo bytes (kept for backward compat). */
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
                .courses(s.getCourses())
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
                .examForm(s.getExamForm())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt());

        if (s.getPhotoData() != null && s.getPhotoData().length > 0) {
            String base64 = Base64.getEncoder().encodeToString(s.getPhotoData());
            String mime   = s.getPhotoMimeType();
            b.photoData(base64);
            b.photoMimeType(mime);
            // Ready-to-use data URI — the frontend sets this directly as img src
            b.studentPhotoUrl("data:" + mime + ";base64," + base64);
        }

        return b.build();
    }
}
