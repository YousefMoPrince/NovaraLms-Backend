package com.amarjo.novaralms.course.repo;

import com.amarjo.novaralms.auth.model.users;
import com.amarjo.novaralms.course.model.EnrollmentStatus;
import com.amarjo.novaralms.course.model.Enrollments;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EnrollmentRepo extends JpaRepository<Enrollments, Long> {

    boolean existsByStudentAndCourseCodeAndEnrollmentStatus(users student, String courseCode, EnrollmentStatus status);

    Optional<Enrollments> findByStudentAndCourseCode(users student, String courseCode);
    Optional<Enrollments> findByStudentAndEnrollmentStatusAndCourseCode(users student, EnrollmentStatus status,String courseCode);

    long countByCourseCodeAndEnrollmentStatus(String courseCode, EnrollmentStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Enrollments e WHERE e.student = :student AND e.courseCode = :courseCode")
    Optional<Enrollments> findByStudentAndCourseCodeForUpdate(@Param("student") users student, @Param("courseCode") String courseCode);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Enrollments e WHERE e.student = :student AND e.courseCode = :courseCode AND e.enrollmentStatus = :enrollmentStatus")
    Optional<Enrollments> findByStudentANDCourseCodeAndEnrollmentStatusForUpdate(@Param("student") users student,@Param("courseCode") String courseCode,@Param("enrollmentStatus") EnrollmentStatus status);

}
