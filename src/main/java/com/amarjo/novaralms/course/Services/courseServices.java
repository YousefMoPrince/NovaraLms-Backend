package com.amarjo.novaralms.course.Services;

import com.amarjo.novaralms.course.DTO.ApiResponse;
import com.amarjo.novaralms.course.DTO.courseRequest;
import com.amarjo.novaralms.course.DTO.courseResponse;
import com.amarjo.novaralms.course.model.Courses;
import com.amarjo.novaralms.course.model.courseVideo;
import com.amarjo.novaralms.course.repo.courseRepo;
import com.amarjo.novaralms.course.repo.videoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class courseServices {
    @Autowired
    private courseRepo CourseRepo;
    private videoRepo VideoRepo;


    public ApiResponse<courseResponse> createCourse(courseRequest request) {
        Courses course = new Courses();
        course.setCourseName(request.getCourseName());
        course.setDepartment(request.getDepartment());
        course.setShortDescription(request.getShortDescription());
        course.setInstructorCode(request.getInstructorCode());
        String courseCode = courseUtility.generateCourseCode(request.getDepartment());
        course.setCourseCode(courseCode);
        CourseRepo.save(course);
        courseResponse response = courseResponse.builder().courseCode(course.getCourseCode()).build();
        return new ApiResponse<>("course created", response);
    }
    public ApiResponse<String> uploadThumbnail(String courseCode,MultipartFile file)throws IOException {
    String fileName = courseCode + ".jpg";
        Path path = Paths.get("storage/Thumbnail/courseThumbnail/" + fileName);
        Path parentPath = path.getParent();
        if (parentPath != null && Files.notExists(parentPath)) {
            Files.createDirectories(parentPath);
        }
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        Courses course = CourseRepo.findByCourseCode(courseCode).orElseThrow(()->new RuntimeException("Course not found"));
        course.setThumbnailPath(path.toString());
        CourseRepo.save(course);
        return new ApiResponse<>("course thumbnail uploaded", path.toString());
    }
    public ApiResponse<courseResponse> updateCreatedCourse(String courseCode, courseRequest request) {
        Courses course = CourseRepo.findByCourseCode(courseCode).orElseThrow(() -> new RuntimeException("Course not found"));
        if (course == null) {
            throw new RuntimeException("Course not found");
        }
        course.setCourseDescription(request.getCourseDescription());
        course.setCourseHours(0.0);
        course.setCreatedAt(LocalDateTime.now());
        CourseRepo.save(course);
        courseResponse response = courseResponse.builder().courseName(request.getCourseName()).courseCode(courseCode).shortDescription(request.getShortDescription()).courseDescription(request.getCourseDescription()).thumbnailPath(request.getThumbnailPath()).courseHours(request.getCourseHours()).createdAt(request.getCreatedAt()).instructorCode(request.getInstructorCode()).build();
        return new ApiResponse<>("course creation complete", response);
    }
    public ApiResponse<courseResponse> updateCourse(String courseCode, courseRequest request) {
        Courses course = CourseRepo.findByCourseCode(courseCode).orElseThrow(()-> new RuntimeException("Course not found"));
        if (course == null) {
            throw new RuntimeException("Course not found");
        }

        course.setCourseName(request.getCourseName());
        course.setDepartment(request.getDepartment());
        course.setShortDescription(request.getShortDescription());
        course.setCourseDescription(request.getCourseDescription());
        CourseRepo.save(course);

        courseResponse response = courseResponse.builder()
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .department(course.getDepartment())
                .shortDescription(course.getShortDescription())
                .courseDescription(course.getCourseDescription())
                .courseHours(course.getCourseHours())
                .instructorCode(course.getInstructorCode())
                .build();

        return new ApiResponse<>("course updated successfully", response);
    }
    public ApiResponse<courseResponse> deleteCourse(String courseCode) throws IOException {
        Courses course = CourseRepo.findByCourseCode(courseCode).orElseThrow(()-> new RuntimeException("Course not found"));
        if (course == null) {
            throw new RuntimeException("Course not found");
        }
        if (course.getThumbnailPath() != null) {
            Files.deleteIfExists(Paths.get(course.getThumbnailPath()));
        }
        List<courseVideo> videos = VideoRepo.findByCourseCode(courseCode);
        for (courseVideo video : videos) {
            if (video.getVideoPath() != null) Files.deleteIfExists(Paths.get(video.getVideoPath()));
            if (video.getThumbnailPath() != null) Files.deleteIfExists(Paths.get(video.getThumbnailPath()));
        }
        Path courseFolder = Paths.get("storage/courseVideos/" + courseCode);
        if (Files.exists(courseFolder)) {
            FileSystemUtils.deleteRecursively(courseFolder);
        }
        CourseRepo.delete(course);
        courseResponse response = courseResponse.builder().build();
        return new ApiResponse<>("course deleted successfully", response);
    }
    public ApiResponse<courseResponse> getCourse(String courseCode) {
        Courses course = CourseRepo.findByCourseCode(courseCode).orElseThrow(()-> new RuntimeException("Course not found"));
        if (course == null) {
            throw new RuntimeException("Course not found");
        }
        courseResponse response = courseResponse.builder()
                .courseCode(course.getCourseCode())
                .department(course.getDepartment())
                .courseName(course.getCourseName())
                .shortDescription(course.getShortDescription())
                .courseDescription(course.getCourseDescription())
                .thumbnailPath(course.getThumbnailPath())
                .courseHours(course.getCourseHours())
                .createdAt(String.valueOf(course.getCreatedAt()))
                .instructorCode(course.getInstructorCode())
                .build();
        return new ApiResponse<>("course get successfully", response);
    }
    public ApiResponse<List<courseResponse>> getAllCourses() {
        List<Courses> courses = CourseRepo.findAll();

        List<courseResponse> responses = courses.stream()
                .map(course -> courseResponse.builder()
                        .courseCode(course.getCourseCode())
                        .courseName(course.getCourseName())
                        .department(course.getDepartment())
                        .shortDescription(course.getShortDescription())
                        .thumbnailPath(course.getThumbnailPath())
                        .courseHours(course.getCourseHours())
                        .createdAt(String.valueOf(course.getCreatedAt()))
                        .instructorCode(course.getInstructorCode())
                        .build())
                .toList();

        return new ApiResponse<>("all courses fetched", responses);
    }
    public ApiResponse<List<courseResponse>> getCoursesByInstructor(String instructorCode) {
        List<Courses> coursesList = CourseRepo.findCoursesByInstructorCode(instructorCode).orElseThrow(()->new RuntimeException("Courses not found"));
        List<courseResponse> responses = coursesList.stream()
                .map(course -> courseResponse.builder()
                        .courseCode(course.getCourseCode())
                        .courseName(course.getCourseName())
                        .department(course.getDepartment())
                        .shortDescription(course.getShortDescription())
                        .thumbnailPath(course.getThumbnailPath())
                        .courseHours(course.getCourseHours())
                        .createdAt(String.valueOf(course.getCreatedAt()))
                        .instructorCode(course.getInstructorCode())
                        .build())
                .toList();

        return new ApiResponse<>("all Instructor's courses fetched", responses);
    }
}
