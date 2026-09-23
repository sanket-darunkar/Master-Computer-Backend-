package com.mastercomputeracademy.service;

import com.mastercomputeracademy.dto.request.CreateStudentRequest;
import com.mastercomputeracademy.dto.request.UpdateStudentRequest;
import com.mastercomputeracademy.dto.request.UpdateStudentStatusRequest;
import com.mastercomputeracademy.dto.response.PagedResponse;
import com.mastercomputeracademy.dto.response.StudentResponse;
import com.mastercomputeracademy.entity.Student.StudentStatus;
import org.springframework.web.multipart.MultipartFile;

public interface StudentService {

    StudentResponse createStudent(CreateStudentRequest request, MultipartFile photo);

    PagedResponse<StudentResponse> getStudents(
            int page, int size, String search, StudentStatus status, String course);

    StudentResponse getStudentById(Long id);

    StudentResponse updateStudent(Long id, UpdateStudentRequest request, MultipartFile photo);

    StudentResponse updateStudentStatus(Long id, UpdateStudentStatusRequest request);
}
