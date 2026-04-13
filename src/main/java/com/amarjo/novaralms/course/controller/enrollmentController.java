package com.amarjo.novaralms.course.controller;

import com.amarjo.novaralms.course.DTO.ApiResponse;
import com.amarjo.novaralms.course.DTO.enrollmentRequest;
import com.amarjo.novaralms.course.DTO.enrollmentResponse;
import com.amarjo.novaralms.course.Services.enrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/enroll")
public class enrollmentController {
    @Autowired
    private enrollmentService EnrollmentService;
    @PostMapping("/enrollstudent")
    public ResponseEntity<ApiResponse<enrollmentResponse>> enrollStudent(@RequestBody enrollmentRequest request, Authentication auth) {
    String email = auth.getName();
        return new ResponseEntity<>(EnrollmentService.enrollStudent(request,email), HttpStatus.CREATED);
    }
    @GetMapping("/{courseCode}/enrollmentcount")
    public ResponseEntity<ApiResponse<Long>> getEnrollmentCount(@PathVariable String courseCode) {
        return new ResponseEntity<>(EnrollmentService.getEnrollmentCountByCourse(courseCode), HttpStatus.OK);
    }
}
