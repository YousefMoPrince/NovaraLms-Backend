package com.amarjo.novaralms.course.controller;

import com.amarjo.novaralms.course.DTO.CourseApiResponse;
import com.amarjo.novaralms.course.DTO.CourseRequest;
import com.amarjo.novaralms.course.DTO.CourseResponse;
import com.amarjo.novaralms.course.Services.CourseServices;
import com.amarjo.novaralms.course.model.CourseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/mob/v1/course")
public class MobileCourseController {

    @Autowired
    private CourseServices courseServices;
    public static final Character c = 'M';

    @PostMapping("/createCourse")
    public ResponseEntity<CourseApiResponse<CourseResponse>> createCourse(@RequestBody CourseRequest courseRequest) {
        return new ResponseEntity<>(new CourseApiResponse<>("Course Created",courseServices.createCourse(courseRequest, c)), HttpStatus.CREATED);
    }

    @PostMapping("/{courseCode}/thumbnail")
    public ResponseEntity<CourseApiResponse<String>> uploadThumbnail(@PathVariable String courseCode, @RequestParam("file") MultipartFile file) throws IOException {
        return new ResponseEntity<>(courseServices.uploadThumbnail(courseCode, file, c), HttpStatus.OK);
    }

    @PatchMapping("/{courseCode}/publish")
    public ResponseEntity<CourseApiResponse<String>> completeCourse(@PathVariable String courseCode) {
        return new ResponseEntity<>(new CourseApiResponse<>("Course Published",courseServices.publishCourse(courseCode, c)), HttpStatus.OK);
    }

    @PutMapping("/{courseCode}/update")
    public ResponseEntity<CourseApiResponse<String>> updateCourse(@PathVariable String courseCode, @RequestBody CourseRequest courseRequest) {
        courseServices.updateCourse(courseCode, courseRequest, c);
        return new ResponseEntity<>(new CourseApiResponse<>("Course Updated",null), HttpStatus.OK);
    }

    @PatchMapping("/{courseCode}/archive")
    public ResponseEntity<CourseApiResponse<String>> archiveCourse(@PathVariable String courseCode) {
        courseServices.archiveCourse(courseCode, c);
        return new ResponseEntity<>(new CourseApiResponse<>("Course Archived",null), HttpStatus.OK);
    }

    @DeleteMapping("/{courseCode}/delete")
    public ResponseEntity<CourseApiResponse<String>> deleteCourse(@PathVariable String courseCode) throws IOException {
        courseServices.deleteCourse(courseCode, c);
        return new ResponseEntity<>(new CourseApiResponse<>("Course Deleted Successfully",null), HttpStatus.OK);
    }

    @GetMapping("/{courseCode}/getCourse")
    public ResponseEntity<CourseApiResponse<CourseResponse>> getCourse(@PathVariable String courseCode) {
        return new ResponseEntity<>(courseServices.getCourse(courseCode), HttpStatus.OK);
    }

    @GetMapping("/instructorCourses/{instructorCode}")
    public ResponseEntity<CourseApiResponse<List<CourseResponse>>> getCoursesByInstructor(@PathVariable String instructorCode) {
        return new ResponseEntity<>(courseServices.getCoursesByInstructor(instructorCode), HttpStatus.OK);
    }

    @GetMapping("/courses/{status}")
    public ResponseEntity<CourseApiResponse<List<CourseResponse>>> getAllCoursesByStatus(@PathVariable CourseStatus status) {
        return new ResponseEntity<>(courseServices.getAllCoursesByStatus(status), HttpStatus.OK);
    }

    @GetMapping("/courses/{department}")
    public ResponseEntity<CourseApiResponse<List<CourseResponse>>> getAllCoursesByDepartment(@PathVariable String department) {
        return new ResponseEntity<>(courseServices.getAllCoursesByDepartment(department), HttpStatus.OK);
    }

}