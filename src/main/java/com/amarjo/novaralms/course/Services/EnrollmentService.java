package com.amarjo.novaralms.course.Services;

import com.amarjo.novaralms.auth.Services.SessionService;
import com.amarjo.novaralms.auth.model.users;
import com.amarjo.novaralms.auth.repository.UserRepo;
import com.amarjo.novaralms.course.DTO.CourseApiResponse;
import com.amarjo.novaralms.course.DTO.EnrollmentRequest;
import com.amarjo.novaralms.course.DTO.EnrollmentResponse;
import com.amarjo.novaralms.course.model.Courses;
import com.amarjo.novaralms.course.model.EnrollmentStatus;
import com.amarjo.novaralms.course.model.Enrollments;
import com.amarjo.novaralms.course.model.StudentStatus;
import com.amarjo.novaralms.course.repo.CourseRepo;
import com.amarjo.novaralms.course.repo.EnrollmentRepo;
import com.amarjo.novaralms.payment.Model.PaymentStatus;
import com.amarjo.novaralms.payment.Repo.PaymentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class EnrollmentService {
    @Autowired
    private EnrollmentRepo enrollmentRepo;
    @Autowired
    private UserRepo UserRepo;
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private PaymentRepo paymentRepo;

    private String userAuth(Character c){
        String email;
        if (c == 'W') {
            if (sessionService.isSessionActive()) {
                email = sessionService.getEmail();
            } else {
                throw new RuntimeException("Unauthorized: No active Web Session found");
            }
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                email = auth.getName();
            } else {
                throw new RuntimeException("Unauthorized: Invalid or missing Mobile JWT");
            }
        }
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Unauthorized: Invalid or missing Auth");
        }
        return email;
    }

    @Transactional
    public EnrollmentResponse enrollStudent(EnrollmentRequest request, Character c) {
        String email = userAuth(c);
                users student = UserRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        if (enrollmentRepo.existsByStudentAndCourseCodeAndEnrollmentStatus(student, request.getCourseCode(), EnrollmentStatus.ACTIVE)) {
            throw new RuntimeException("You are already enrolled in this course");
        }

        Enrollments enrollment = Enrollments.builder()
                .courseCode(request.getCourseCode())
                .enrollmentStatus(EnrollmentStatus.PENDING)
                .studentStatus(StudentStatus.NOT_ENROLLED)
                .instructorCode(request.getInstructorCode())
                .contentFinished(0)
                .student(student)
                .build();

        enrollmentRepo.save(enrollment);

        EnrollmentResponse response = EnrollmentResponse.builder()
                .enrollmentId(enrollment.getEnrollmentId())
                .enrollmentStatus(enrollment.getEnrollmentStatus())
                .studentStatus(enrollment.getStudentStatus())
                .contentFinished(enrollment.getContentFinished())
                .courseCode(enrollment.getCourseCode())
                .instructorCode(enrollment.getInstructorCode())
                .studentId(student.getUserId())
                .build();

        return  response;
    }
    @Transactional(isolation = Isolation.REPEATABLE_READ,propagation = Propagation.REQUIRED)
    public void enroll(String courseCode, Character c) {
        String email = userAuth(c);
        users user = UserRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        Courses course = courseRepo.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Course not found"));
        boolean isReadyForEnrollment = paymentRepo.existsByPayerAccountNumberAndCourseCodeAndPaymentStatus(user.getAccountNumber(), course.getCourseCode(),PaymentStatus.SUCCESS);
        if (!isReadyForEnrollment) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You are not ready to enroll in this course");
        }
        try {
            Enrollments enrollment = enrollmentRepo.findByStudentAndCourseCodeForUpdate(user,course.getCourseCode())
                    .orElseThrow(() -> new RuntimeException("Enrollment not found"));
            enrollment.setStudentStatus(StudentStatus.IN_PROGRESS);
            enrollment.setEnrollmentDate(LocalDateTime.now());
            enrollmentRepo.save(enrollment);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public CourseApiResponse<Long> getEnrollmentCountByCourse(String courseCode) {
        long count = enrollmentRepo.countByCourseCodeAndEnrollmentStatus(courseCode,EnrollmentStatus.ACTIVE);
        return new CourseApiResponse<>("Enrollment count fetched successfully", count);
    }

}
