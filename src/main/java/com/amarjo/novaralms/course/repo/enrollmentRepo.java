package com.amarjo.novaralms.course.repo;

import com.amarjo.novaralms.auth.model.users;
import com.amarjo.novaralms.course.model.enrollments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface enrollmentRepo extends JpaRepository<enrollments, String> {
    boolean existsByStudentIdAndCourseCode(users student, String courseCode);
    long countByCourseCode(String courseCode);

}
