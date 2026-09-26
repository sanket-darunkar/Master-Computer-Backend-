package com.mastercomputeracademy.service;

import com.mastercomputeracademy.dto.request.CreateStudentRequest;
import com.mastercomputeracademy.dto.request.UpdateStudentRequest;
import com.mastercomputeracademy.dto.request.UpdateStudentStatusRequest;
import com.mastercomputeracademy.dto.response.PagedResponse;
import com.mastercomputeracademy.dto.response.PublicStudentResponse;
import com.mastercomputeracademy.dto.response.StudentResponse;
import org.springframework.web.multipart.MultipartFile;

public interface StudentService {

    StudentResponse createStudent(CreateStudentRequest request, MultipartFile photo);

    PagedResponse<StudentResponse> getStudents(
            int page, int size, String search, String examFormFilter, String course);

    StudentResponse getStudentById(Long id);

    StudentResponse updateStudent(Long id, UpdateStudentRequest request, MultipartFile photo);

    StudentResponse updateStudentStatus(Long id, UpdateStudentStatusRequest request);

    /**
     * Public lookup by human-readable studentId + mobile verification.
     * Returns a PII-safe response — no Aadhaar, address, or fee data.
     * Throws ResourceNotFoundException if studentId does not exist.
     * Throws BadCredentialsException if the mobile does not match.
     */
    PublicStudentResponse lookupStudentByStudentId(String studentId, String mobile);
}
