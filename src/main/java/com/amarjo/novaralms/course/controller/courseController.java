package com.amarjo.novaralms.course.controller;

import com.amarjo.novaralms.course.DTO.ApiResponse;
import com.amarjo.novaralms.course.DTO.courseRequest;
import com.amarjo.novaralms.course.DTO.courseResponse;
import com.amarjo.novaralms.course.Services.courseServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/course")
public class courseController {
    @Autowired
    private courseServices CourseServices;
    @PostMapping("/createCourse")
    public ResponseEntity<ApiResponse<courseResponse>> createCourse(@RequestBody courseRequest CourseRequest) {
        return new ResponseEntity<>(CourseServices.createCourse(CourseRequest), HttpStatus.CREATED);
    }
    @PostMapping("/{courseCode}/thumbnail")
    public ResponseEntity<ApiResponse<String>> uploadThumbnail(@PathVariable String courseCode,@RequestParam("file") MultipartFile file)
            throws IOException {
        return new ResponseEntity<>(CourseServices.uploadThumbnail(courseCode, file), HttpStatus.OK);
    }
    @PatchMapping("/{courseCode}/complete")
    public ResponseEntity<ApiResponse<courseResponse>> completeCourse(@PathVariable String courseCode,@RequestBody courseRequest CourseRequest) {
        return new ResponseEntity<>(CourseServices.updateCreatedCourse(courseCode, CourseRequest), HttpStatus.OK);
    }
    @PatchMapping("/{courseCode}/update")
    public ResponseEntity<ApiResponse<courseResponse>> updateCourse(@PathVariable String courseCode,@RequestBody courseRequest CourseRequest) {
        return new ResponseEntity<>(CourseServices.updateCourse(courseCode, CourseRequest), HttpStatus.OK);
    }
    @DeleteMapping("/{courseCode}/delete")
    public ResponseEntity<ApiResponse<courseResponse>> deleteCourse(@PathVariable String courseCode) throws IOException {
        return new ResponseEntity<>(CourseServices.deleteCourse(courseCode), HttpStatus.OK);
    }
    @GetMapping("/{courseCode}/getCourse")
    public ResponseEntity<ApiResponse<courseResponse>> getCourse(@PathVariable String courseCode) {
        return new ResponseEntity<>(CourseServices.getCourse(courseCode), HttpStatus.OK);
    }
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<courseResponse>>> getAllCourses() {
        return new ResponseEntity<>(CourseServices.getAllCourses(), HttpStatus.OK);
    }
    @GetMapping("/instructorCourses/{instructorCode}")
    public ResponseEntity<ApiResponse<List<courseResponse>>> getCoursesByInstructor(@PathVariable String instructorCode) {
        return new ResponseEntity<>(CourseServices.getCoursesByInstructor(instructorCode), HttpStatus.OK);
    }

}
