package com.mastercomputeracademy.service.impl;

import com.mastercomputeracademy.dto.request.CreateStudentRequest;
import com.mastercomputeracademy.dto.request.UpdateStudentRequest;
import com.mastercomputeracademy.dto.request.UpdateStudentStatusRequest;
import com.mastercomputeracademy.dto.response.PagedResponse;
import com.mastercomputeracademy.dto.response.PublicStudentResponse;
import com.mastercomputeracademy.dto.response.StudentResponse;
import com.mastercomputeracademy.entity.Student;
import com.mastercomputeracademy.entity.Student.StudentStatus;
import com.mastercomputeracademy.exception.BadCredentialsException;
import com.mastercomputeracademy.exception.DuplicateStudentIdException;
import com.mastercomputeracademy.exception.ResourceNotFoundException;
import com.mastercomputeracademy.repository.StudentRepository;
import com.mastercomputeracademy.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private static final long    MAX_PHOTO_BYTES = 2 * 1024 * 1024L; // 2 MB
    private static final Set<String> ALLOWED_MIME = Set.of("image/jpeg", "image/png");
    private static final Set<String> ALLOWED_EXT  = Set.of("jpg", "jpeg", "png");

    private final StudentRepository studentRepository;

    // ------------------------------------------------------------------
    // Create
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request, MultipartFile photo) {
        log.debug("Creating student: {}", request.getStudentId());

        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new DuplicateStudentIdException(request.getStudentId());
        }

        Student student = Student.builder()
                .studentId(request.getStudentId())
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .surname(request.getSurname())
                .applicantName(request.getApplicantName())
                .motherName(request.getMotherName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .maritalStatus(request.getMaritalStatus())
                .aadhaarNumber(request.getAadhaarNumber())
                .ownMobile(request.getOwnMobile())
                .otherMobile(request.getOtherMobile())
                .houseNo(request.getHouseNo())
                .street(request.getStreet())
                .city(request.getCity())
                .tahsil(request.getTahsil())
                .district(request.getDistrict())
                .pinCode(request.getPinCode())
                .qualification(request.getQualification())
                .category(request.getCategory())
                .course(request.getCourse())
                .courses(normaliseCourses(request.getCourses(), request.getCourse()))
                .admissionDate(request.getAdmissionDate())
                .courseDuration(request.getCourseDuration())
                .batchTime(request.getBatchTime())
                .totalFees(request.getTotalFees())
                .feesPaid(request.getFeesPaid())
                .receiptNumber(request.getReceiptNumber())
                .receiptDate(request.getReceiptDate())
                .notes(request.getNotes())
                .status(StudentStatus.ACTIVE)
                .examForm("Exam Form Pending")
                .build();

        // Initialise per-course exam statuses — every course starts as Pending
        student.setCourseExamStatuses(
                buildInitialExamStatuses(student.getCourses()));

        applyPhoto(student, photo);

        Student saved = studentRepository.save(student);
        log.info("Student created: id={}, studentId={}", saved.getId(), saved.getStudentId());
        return StudentResponse.fromEntity(saved);
    }

    // ------------------------------------------------------------------
    // List / Search
    // ------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StudentResponse> getStudents(
            int page, int size, String search, String examFormFilter, String course) {

        String searchParam    = (search          != null && !search.isBlank())          ? search.trim()          : "";
        String examFormParam  = (examFormFilter   != null && !examFormFilter.isBlank())  ? examFormFilter.trim()  : "";
        String courseParam    = (course           != null && !course.isBlank())          ? course.trim()          : "";

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Student> resultPage = studentRepository
                .searchStudents(searchParam, examFormParam, courseParam, pageable);

        return PagedResponse.from(resultPage.map(StudentResponse::fromEntity));
    }

    // ------------------------------------------------------------------
    // Get by ID
    // ------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        return StudentResponse.fromEntity(findById(id));
    }

    // ------------------------------------------------------------------
    // Update
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, UpdateStudentRequest request, MultipartFile photo) {
        log.debug("Updating student id={}", id);
        Student student = findById(id);

        student.setFirstName(request.getFirstName());
        student.setMiddleName(request.getMiddleName());
        student.setSurname(request.getSurname());
        student.setApplicantName(request.getApplicantName());
        student.setMotherName(request.getMotherName());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setGender(request.getGender());
        student.setMaritalStatus(request.getMaritalStatus());
        student.setAadhaarNumber(request.getAadhaarNumber());
        student.setOwnMobile(request.getOwnMobile());
        student.setOtherMobile(request.getOtherMobile());
        student.setHouseNo(request.getHouseNo());
        student.setStreet(request.getStreet());
        student.setCity(request.getCity());
        student.setTahsil(request.getTahsil());
        student.setDistrict(request.getDistrict());
        student.setPinCode(request.getPinCode());
        student.setQualification(request.getQualification());
        student.setCategory(request.getCategory());
        student.setCourse(request.getCourse());
        List<String> newCourses = normaliseCourses(request.getCourses(), request.getCourse());
        student.setCourses(newCourses);
        // Reconcile per-course exam statuses: preserve existing statuses for
        // courses that remain; add Pending for newly added courses; drop removed ones.
        student.setCourseExamStatuses(
                reconcileExamStatuses(student.getCourseExamStatuses(), newCourses));
        student.syncLegacyExamForm();
        student.setAdmissionDate(request.getAdmissionDate());
        student.setCourseDuration(request.getCourseDuration());
        student.setBatchTime(request.getBatchTime());
        student.setTotalFees(request.getTotalFees());
        student.setFeesPaid(request.getFeesPaid());
        student.setReceiptNumber(request.getReceiptNumber());
        student.setReceiptDate(request.getReceiptDate());
        student.setNotes(request.getNotes());

        if (request.getStatus() != null) {
            student.setStatus(request.getStatus());
        }
        // examForm from the edit form (global override, only if explicitly set)
        if (request.getExamForm() != null && !request.getExamForm().isBlank()) {
            // Apply to all courses in the map, then sync legacy field
            newCourses.forEach(c ->
                    student.setExamFormForCourse(c, request.getExamForm()));
            student.syncLegacyExamForm();
        }

        // Only replace photo if a new file was uploaded
        if (photo != null && !photo.isEmpty()) {
            applyPhoto(student, photo);
        }

        Student saved = studentRepository.save(student);
        log.info("Student updated: id={}", saved.getId());
        return StudentResponse.fromEntity(saved);
    }

    // ------------------------------------------------------------------
    // Status change (exam form) — per-course or global
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public StudentResponse updateStudentStatus(Long id, UpdateStudentStatusRequest request) {
        Student student = findById(id);
        String newStatus  = request.getExamForm();
        String courseName = (request.getCourseName() != null && !request.getCourseName().isBlank())
                ? request.getCourseName().trim()
                : null;

        if (courseName != null) {
            // ── Per-course update ──────────────────────────────────────
            // Validate the course actually belongs to this student
            List<String> enrolled = student.getCourses();
            if (enrolled == null || !enrolled.contains(courseName)) {
                throw new ResourceNotFoundException(
                        "Course '" + courseName + "' is not enrolled for student id=" + id);
            }
            student.setExamFormForCourse(courseName, newStatus);
            log.info("Per-course examForm updated: studentId={}, course={}, status={}",
                    id, courseName, newStatus);
        } else {
            // ── Global update (backward compat) ────────────────────────
            // Apply status to every enrolled course
            List<String> enrolled = student.getCourses();
            if (enrolled != null && !enrolled.isEmpty()) {
                enrolled.forEach(c -> student.setExamFormForCourse(c, newStatus));
            }
            log.info("Global examForm updated: studentId={}, status={}", id, newStatus);
        }

        // Keep the legacy single field in sync
        student.syncLegacyExamForm();

        Student saved = studentRepository.save(student);
        return StudentResponse.fromEntity(saved);
    }

    // ------------------------------------------------------------------
    // Public lookup (no auth – mobile used as second factor)
    // ------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PublicStudentResponse lookupStudentByStudentId(String studentId, String mobile) {
        log.debug("Public lookup: studentId={}", studentId);

        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No student found with ID: " + studentId));

        // Verify the supplied mobile matches either registered number.
        // Normalise both sides: strip leading/trailing spaces.
        String supplied = mobile == null ? "" : mobile.trim();
        boolean mobileMatches =
                (student.getOwnMobile()   != null && student.getOwnMobile().trim().equals(supplied)) ||
                (student.getOtherMobile() != null && student.getOtherMobile().trim().equals(supplied));

        if (!mobileMatches) {
            // Intentionally vague — do not reveal which field failed
            throw new BadCredentialsException(
                    "Student ID and Mobile Number do not match. Please check and try again.");
        }

        return PublicStudentResponse.fromEntity(student);
    }

    // ------------------------------------------------------------------
    // Photo validation (shared logic mirrors CertificateServiceImpl)
    // ------------------------------------------------------------------

    private void applyPhoto(Student student, MultipartFile photo) {
        if (photo == null || photo.isEmpty()) return;

        if (photo.getSize() > MAX_PHOTO_BYTES) {
            throw new IllegalArgumentException(
                "Photo file is too large. Maximum allowed size is 2 MB.");
        }

        String contentType = photo.getContentType();
        if (contentType == null || !ALLOWED_MIME.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                "Invalid photo type. Only JPG and PNG images are accepted.");
        }

        String originalFilename = photo.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1)
                                         .toLowerCase();
            if (!ext.matches("[a-z0-9]+") || !ALLOWED_EXT.contains(ext)) {
                throw new IllegalArgumentException(
                    "Invalid photo file extension. Only .jpg, .jpeg, and .png are accepted.");
            }
        }

        try {
            byte[] bytes = photo.getBytes();
            student.setPhotoData(bytes);
            student.setPhotoMimeType(contentType.toLowerCase());
            log.debug("Student photo stored: {} bytes, type={}", bytes.length, contentType);
        } catch (IOException e) {
            log.error("Failed to read student photo bytes", e);
            throw new IllegalArgumentException("Failed to read photo file. Please try again.");
        }
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + id));
    }

    /**
     * Ensures the courses list is always populated.
     * If the frontend sends a non-empty courses[], use that.
     * Otherwise fall back to wrapping the single course string.
     * Always returns a mutable, non-null list.
     */
    private List<String> normaliseCourses(List<String> courses, String course) {
        if (courses != null && !courses.isEmpty()) {
            return new java.util.ArrayList<>(courses);
        }
        if (course != null && !course.isBlank()) {
            return new java.util.ArrayList<>(List.of(course));
        }
        return new java.util.ArrayList<>();
    }

    /**
     * Builds a fresh courseExamStatuses map for a newly created student.
     * Every course starts with "Exam Form Pending".
     */
    private Map<String, String> buildInitialExamStatuses(List<String> courses) {
        Map<String, String> map = new LinkedHashMap<>();
        if (courses != null) {
            courses.forEach(c -> map.put(c, "Exam Form Pending"));
        }
        return map;
    }

    /**
     * Reconciles the existing per-course exam statuses when a student's
     * course list is edited.
     *
     * Rules:
     *   – Course retained: keep its existing status unchanged.
     *   – Course added:    initialise to "Exam Form Pending".
     *   – Course removed:  drop its entry silently.
     *
     * @param existing  the current courseExamStatuses map (may be null/empty)
     * @param newCourses the updated list of enrolled courses
     * @return a new LinkedHashMap with order matching newCourses
     */
    private Map<String, String> reconcileExamStatuses(
            Map<String, String> existing, List<String> newCourses) {

        Map<String, String> result = new LinkedHashMap<>();
        if (newCourses == null) return result;
        for (String course : newCourses) {
            String status = (existing != null && existing.containsKey(course))
                    ? existing.get(course)
                    : "Exam Form Pending";
            result.put(course, status);
        }
        return result;
    }
}
