package com.amarjo.novaralms.course.Services;

import com.amarjo.novaralms.auth.model.users;
import com.amarjo.novaralms.auth.repository.userRepo;
import com.amarjo.novaralms.course.DTO.ApiResponse;
import com.amarjo.novaralms.course.DTO.enrollmentRequest;
import com.amarjo.novaralms.course.DTO.enrollmentResponse;
import com.amarjo.novaralms.course.model.enrollments;
import com.amarjo.novaralms.course.repo.enrollmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class enrollmentService {
    @Autowired
    private enrollmentRepo EnrollmentRepo;
    @Autowired
    private userRepo UserRepo;

    public ApiResponse<enrollmentResponse> enrollStudent(enrollmentRequest request, String email) {
        users student = UserRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        if (EnrollmentRepo.existsByStudentIdAndCourseCode(student, request.getCourseCode())) {
            throw new RuntimeException("You are already enrolled in this course");
        }

        enrollments enrollment = enrollments.builder()
                .courseCode(request.getCourseCode())
                .instructorCode(request.getInstructorCode())
                .studentId(student)
                .enrollmentDate(new Date())
                .build();

        EnrollmentRepo.save(enrollment);

        enrollmentResponse response = enrollmentResponse.builder()
                .enrollmentId(enrollment.getEnrollmentId())
                .courseCode(enrollment.getCourseCode())
                .instructorCode(enrollment.getInstructorCode())
                .studentId(student.getUserId())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .build();

        return new ApiResponse<>("Student enrolled successfully", response);
    }
    public ApiResponse<Long> getEnrollmentCountByCourse(String courseCode) {
        long count = EnrollmentRepo.countByCourseCode(courseCode);
        return new ApiResponse<>("Enrollment count fetched successfully", count);
    }

}
