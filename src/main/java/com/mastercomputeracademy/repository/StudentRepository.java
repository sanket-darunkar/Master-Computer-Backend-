package com.mastercomputeracademy.repository;

import com.mastercomputeracademy.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentId(String studentId);

    boolean existsByStudentId(String studentId);

    /**
     * Paginated search across studentId, firstName, surname, and ownMobile.
     *
     * examFormFilter – exact match on the examForm field
     *   ('Exam Form Submitted' | 'Exam Form Pending').
     *   Pass empty string to skip filtering.
     *
     * All filter parameters are optional — pass empty string to skip.
     * Uses database-level LIKE; never loads the full table into memory.
     */
    @Query("""
            SELECT s FROM Student s
            WHERE (:search = '' OR
                   LOWER(s.studentId)  LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(s.firstName)  LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(s.surname)    LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(s.ownMobile)  LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:examFormFilter = '' OR s.examForm = :examFormFilter)
              AND (:course = '' OR LOWER(s.course) LIKE LOWER(CONCAT('%', :course, '%')))
            """)
    Page<Student> searchStudents(
            @Param("search")          String search,
            @Param("examFormFilter")  String examFormFilter,
            @Param("course")          String course,
            Pageable pageable);
}
