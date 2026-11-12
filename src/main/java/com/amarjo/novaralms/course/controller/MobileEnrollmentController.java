package com.amarjo.novaralms.course.controller;

import com.amarjo.novaralms.course.DTO.CourseApiResponse;
import com.amarjo.novaralms.course.DTO.EnrollmentRequest;
import com.amarjo.novaralms.course.DTO.EnrollmentResponse;
import com.amarjo.novaralms.course.Services.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mob/v1/enroll")
public class MobileEnrollmentController {
    @Autowired
    private EnrollmentService enrollmentService;
    public static final Character c = 'M';

    @PostMapping("/pending")
    public ResponseEntity<CourseApiResponse<EnrollmentResponse>> enrollStudent(@RequestBody EnrollmentRequest request) {
        return new ResponseEntity<>(new CourseApiResponse<>("Enrollment Pending",enrollmentService.enrollStudent(request,c)), HttpStatus.CREATED);
    }
    @PostMapping("/{courseCode}/ack")
    public ResponseEntity<String> ackEnrollment(@PathVariable String courseCode) {
        enrollmentService.enroll(courseCode,c);
        return new ResponseEntity<>("Enrollment Successful", HttpStatus.CREATED);
    }
    @GetMapping("/{courseCode}/enrollment/count")
    public ResponseEntity<CourseApiResponse<Long>> getEnrollmentCount(@PathVariable String courseCode) {
        return new ResponseEntity<>(enrollmentService.getEnrollmentCountByCourse(courseCode), HttpStatus.OK);
    }

}
