package com.amarjo.novaralms.course.Services;

import com.amarjo.novaralms.auth.Services.SessionService;
import com.amarjo.novaralms.auth.model.users;
import com.amarjo.novaralms.auth.repository.UserRepo;
import com.amarjo.novaralms.course.DTO.CourseApiResponse;
import com.amarjo.novaralms.course.DTO.CourseRequest;
import com.amarjo.novaralms.course.DTO.CourseResponse;
import com.amarjo.novaralms.course.model.CourseStatus;
import com.amarjo.novaralms.course.model.Courses;
import com.amarjo.novaralms.course.model.CourseVideo;
import com.amarjo.novaralms.course.repo.CourseRepo;
import com.amarjo.novaralms.course.repo.VideoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseServices {
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private VideoRepo VideoRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private SessionService sessionService;

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

    public CourseResponse createCourse(CourseRequest request, Character c) {
        try {
            String email = userAuth(c);
                users user = userRepo.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                Courses course = new Courses();
                String courseCode = courseUtility.generateCourseCode(request.getDepartment());
                course.setCourseCode(courseCode);
                course.setCourseName(request.getCourseName());
                course.setDepartment(request.getDepartment());
                BigDecimal coursePrice = BigDecimal.ZERO;
                if (user.getIsReadyForPayment()){
                    coursePrice = request.getPrice();
                }
                course.setPrice(coursePrice);
                course.setCurrency(request.getCurrency());
                course.setShortDescription(request.getShortDescription());
                course.setCourseDescription(request.getCourseDescription());
                course.setCourseHours(BigDecimal.ZERO);
                course.setCreatedAt(LocalDateTime.now());
                course.setCreatedBy(user.getName());
                course.setInstructorCode(user.getCode());
                course.setStatus(CourseStatus.DRAFT);
                courseRepo.save(course);
                return CourseResponse.builder()
                        .courseCode(course.getCourseCode())
                        .build();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    public CourseApiResponse<String> uploadThumbnail(String courseCode, MultipartFile file,Character c) throws IOException {
        try {
            String email = userAuth(c);
            users user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        Courses course = courseRepo.findByCourseCode(courseCode)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Course thumbnail file is missing or empty");
        }
        if (!user.getCode().equals(course.getInstructorCode())) {
            throw new RuntimeException("Instructor code mismatch to upload thumbnail");
        }
        String fileName = courseCode + ".jpg";
        String uploadDir = System.getProperty("user.dir") + File.separator + "storage" + File.separator + "Thumbnail" + File.separator + "courseThumbnail";

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File destinationFile = new File(directory, fileName);

        try {
            file.transferTo(destinationFile);
        } catch (IOException e) {
            throw new IOException("Critical error during course thumbnail upload: " + e.getMessage());
        }

        String relativePath = "storage/Thumbnail/courseThumbnail/" + fileName;
        course.setThumbnailPath(relativePath);
        course.setUpdatedAt(LocalDateTime.now());
            courseRepo.save(course);

        return new CourseApiResponse<>("Course thumbnail uploaded successfully", relativePath);
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String publishCourse(String courseCode, Character c) {
        try {
            String email = userAuth(c);
            users user = userRepo.findByEmail(email).orElseThrow(
                    () -> new RuntimeException("User not found"));
            Courses course = courseRepo.findByCourseCode(courseCode).orElseThrow(
                    () -> new RuntimeException("Course not found"));
            if (!user.getCode().equals(course.getInstructorCode())) {
                throw new RuntimeException("Instructor code mismatch to upload thumbnail");
            }
            course.setStatus(CourseStatus.PUBLISHED);
            courseRepo.save(course);
            return "Course published successfully";
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    public void updateCourse(String courseCode, CourseRequest request, Character c) {
        try {
            String email = userAuth(c);
            users user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Courses course = courseRepo.findByCourseCode(courseCode).orElseThrow(()-> new RuntimeException("Course not found"));
        if (course == null) {
            throw new RuntimeException("Course not found");
        }
        if (!user.getCode().equals(course.getInstructorCode())) {
            throw new RuntimeException("Instructor code mismatch");
        }
        if (request.getCourseName() != null) course.setCourseName(request.getCourseName());
        if (request.getPrice() != null) course.setPrice(request.getPrice());
        if (request.getShortDescription() != null) course.setShortDescription(request.getShortDescription());
        if (request.getCourseDescription() != null) course.setCourseDescription(request.getCourseDescription());
        course.setStatus(CourseStatus.UPDATED);
        course.setUpdatedAt(LocalDateTime.now());
            courseRepo.save(course);
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    public void archiveCourse(String courseCode, Character c) {
        try {
            String email = userAuth(c);
            users user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
            Courses course = courseRepo.findByCourseCode(courseCode).orElseThrow(() -> new RuntimeException("Course not found"));
            if (course == null) {
                throw new RuntimeException("Course not found");
            }
            if (!user.getCode().equals(course.getInstructorCode())) {
                throw new RuntimeException("Instructor code mismatch");
            }
            course.setStatus(CourseStatus.ARCHIVED);
            courseRepo.save(course);
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    public void deleteCourse(String courseCode, Character c) throws IOException {
        try {
            String email = userAuth(c);
            users user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
            Courses course = courseRepo.findByCourseCode(courseCode).orElseThrow(() -> new RuntimeException("Course not found"));
            if (course == null) {
                throw new RuntimeException("Course not found");
            }
            if (!user.getCode().equals(course.getInstructorCode())) {
                throw new RuntimeException("Instructor code mismatch");
            }
            if (course.getThumbnailPath() != null) {
                Files.deleteIfExists(Paths.get(course.getThumbnailPath()));
            }
            List<CourseVideo> videos = VideoRepo.findByCourseCode(courseCode);
            for (CourseVideo video : videos) {
                if (video.getVideoPath() != null) Files.deleteIfExists(Paths.get(video.getVideoPath()));
                if (video.getThumbnailPath() != null) Files.deleteIfExists(Paths.get(video.getThumbnailPath()));
            }
            Path courseFolder = Paths.get("storage/courseVideos/" + courseCode);
            if (Files.exists(courseFolder)) {
                FileSystemUtils.deleteRecursively(courseFolder);
            }
            courseRepo.delete(course);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    public CourseApiResponse<CourseResponse> getCourse(String courseCode) {
        Courses course = courseRepo.findByCourseCode(courseCode).orElseThrow(()-> new RuntimeException("Course not found"));
        if (course == null) {
            throw new RuntimeException("Course not found");
        }
        CourseResponse response = CourseResponse.builder()
                .courseCode(course.getCourseCode())
                .department(course.getDepartment())
                .courseName(course.getCourseName())
                .price(course.getPrice())
                .currency(course.getCurrency())
                .shortDescription(course.getShortDescription())
                .courseDescription(course.getCourseDescription())
                .thumbnailPath(course.getThumbnailPath())
                .courseHours(course.getCourseHours())
                .createdAt(String.valueOf(course.getCreatedAt()))
                .updatedAt(String.valueOf(course.getUpdatedAt()))
                .createdBy(course.getCreatedBy())
                .instructorCode(course.getInstructorCode())
                .status(course.getStatus())
                .videoCounter(course.getVideoCounter())
                .build();
        return new CourseApiResponse<>("course get successfully", response);
    }
    public CourseApiResponse<List<CourseResponse>> getAllCoursesByStatus(CourseStatus status) {
        List<Courses> courses = courseRepo.findCoursesByStatus(status)
                .orElseThrow(()-> new RuntimeException("Course not found"));

        List<CourseResponse> responses = courses.stream()
                .map(course -> CourseResponse.builder()
                        .courseCode(course.getCourseCode())
                        .courseName(course.getCourseName())
                        .department(course.getDepartment())
                        .price(course.getPrice())
                        .currency(course.getCurrency())
                        .shortDescription(course.getShortDescription())
                        .thumbnailPath(course.getThumbnailPath())
                        .courseHours(course.getCourseHours())
                        .createdAt(String.valueOf(course.getCreatedAt()))
                        .createdBy(course.getCreatedBy())
                        .status(course.getStatus())
                        .build())
                .toList();

        return new CourseApiResponse<>("all courses fetched", responses);
    }    public CourseApiResponse<List<CourseResponse>> getAllCoursesByDepartment(String department) {
        List<Courses> courses = courseRepo.findCoursesByDepartment(department)
                .orElseThrow(()-> new RuntimeException("Course not found"));

        List<CourseResponse> responses = courses.stream()
                .map(course -> CourseResponse.builder()
                        .courseCode(course.getCourseCode())
                        .courseName(course.getCourseName())
                        .department(course.getDepartment())
                        .price(course.getPrice())
                        .currency(course.getCurrency())
                        .shortDescription(course.getShortDescription())
                        .thumbnailPath(course.getThumbnailPath())
                        .courseHours(course.getCourseHours())
                        .createdAt(String.valueOf(course.getCreatedAt()))
                        .createdBy(course.getCreatedBy())
                        .build())
                .toList();

        return new CourseApiResponse<>("all department courses fetched", responses);
    }
    public CourseApiResponse<List<CourseResponse>> getCoursesByInstructor(String instructorCode) {
        List<Courses> coursesList = courseRepo.findCoursesByInstructorCode(instructorCode).orElseThrow(()->new RuntimeException("Courses not found"));
        List<CourseResponse> responses = coursesList.stream()
                .map(course -> CourseResponse.builder()
                        .courseCode(course.getCourseCode())
                        .courseName(course.getCourseName())
                        .department(course.getDepartment())
                        .price(course.getPrice())
                        .currency(course.getCurrency())
                        .shortDescription(course.getShortDescription())
                        .thumbnailPath(course.getThumbnailPath())
                        .courseHours(course.getCourseHours())
                        .createdAt(String.valueOf(course.getCreatedAt()))
                        .build())
                .toList();

        return new CourseApiResponse<>("all Instructor's courses fetched", responses);
    }
}
