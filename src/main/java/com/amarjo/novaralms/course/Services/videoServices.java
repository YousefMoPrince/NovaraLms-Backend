package com.amarjo.novaralms.course.Services;

import com.amarjo.novaralms.auth.model.users;
import com.amarjo.novaralms.auth.repository.userRepo;
import com.amarjo.novaralms.course.DTO.ApiResponse;
import com.amarjo.novaralms.course.DTO.videoRequest;
import com.amarjo.novaralms.course.DTO.videoResponse;
import com.amarjo.novaralms.course.model.Courses;
import com.amarjo.novaralms.course.model.courseVideo;
import com.amarjo.novaralms.course.repo.courseRepo;
import com.amarjo.novaralms.course.repo.enrollmentRepo;
import com.amarjo.novaralms.course.repo.videoRepo;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class videoServices {
    @Autowired
    private videoRepo VideoRepo;
    @Autowired
    private courseRepo CourseRepo;
    @Autowired
    private userRepo UserRepo;
    @Autowired
    private enrollmentRepo EnrollRepo;

    public ApiResponse<videoResponse> addVideo(videoRequest request) {
        courseVideo video = courseVideo.builder().courseCode(request.getCourseCode()).Title(request.getTitle()).build();
        VideoRepo.save(video);
        videoResponse response = videoResponse.builder().videoId(video.getVideoId()).build();
        return new ApiResponse<>("Video added", response);
    }
    public ApiResponse<String> uploadVideoThumbnail(String courseCode, String videoId, MultipartFile file) throws IOException {
        Courses course = CourseRepo.findByCourseCode(courseCode)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        courseVideo video = VideoRepo.findByVideoId(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Thumbnail file is missing or empty");
        }

        int index = course.getThumbnailCounter() + 1;
        String fileName = courseCode + "_" + index + ".jpg";

        String projectRoot = System.getProperty("user.dir");
        Path uploadPath = Paths.get(projectRoot, "storage", "Thumbnail", "videoThumbnail", courseCode);

        if (Files.notExists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        File destinationFile = uploadPath.resolve(fileName).toFile();

        try {
            file.transferTo(destinationFile);
        } catch (IOException e) {
            throw new IOException("Critical error: Could not save video thumbnail. " + e.getMessage());
        }

        String dbPath = "storage/Thumbnail/videoThumbnail/" + courseCode + "/" + fileName;

        course.setThumbnailCounter(index);
        CourseRepo.save(course);

        video.setThumbnailPath(dbPath);
        VideoRepo.save(video);

        return new ApiResponse<>("Video thumbnail uploaded successfully", dbPath);
    }
    public ApiResponse<String> uploadVideo(String courseCode, String videoId, MultipartFile file) throws IOException {
        Courses course = CourseRepo.findByCourseCode(courseCode)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        courseVideo video = VideoRepo.findByVideoId(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Failed to upload empty video file");
        }

        int index = course.getVideoCounter() + 1;
        String fileName = courseCode + "_" + index + ".mp4";

        String projectRoot = System.getProperty("user.dir");
        Path uploadPath = Paths.get(projectRoot, "storage", "courseVideos", courseCode);

        if (Files.notExists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        File destinationFile = uploadPath.resolve(fileName).toFile();

        try {
            file.transferTo(destinationFile);
        } catch (IOException e) {
            throw new IOException("Critical error: Could not save video file to disk. " + e.getMessage());
        }

        course.setVideoCounter(index);
        CourseRepo.save(course);

        String dbPath = "storage/courseVideos/" + courseCode + "/" + fileName;
        video.setVideoPath(dbPath);
        VideoRepo.save(video);

        return new ApiResponse<>("Video uploaded successfully", dbPath);
    }
    public ApiResponse<videoResponse> finalizeVideo(String videoId, videoRequest request) {
        courseVideo video = VideoRepo.findByVideoId(videoId).orElseThrow(()->new RuntimeException("Video not found"));
        video.setDescription(request.getDescription());
        video.setDuration(request.getDuration());
        VideoRepo.save(video);

        Courses course = CourseRepo.findByCourseCode(video.getCourseCode()).orElseThrow(() -> new RuntimeException("Course not found"));
        if (course != null) {
            course.setCourseHours(course.getCourseHours() + (video.getDuration()/60.0));
            CourseRepo.save(course);
        }

        videoResponse response = videoResponse.builder()
                .videoId(video.getVideoId())
                .courseCode(video.getCourseCode())
                .Title(video.getTitle())
                .Description(video.getDescription())
                .videoPath(video.getVideoPath())
                .thumbnailPath(video.getThumbnailPath())
                .duration(video.getDuration())
                .build();

        return new ApiResponse<>("video finalized successfully", response);
    }
    public ResourceRegion streamVideo(String videoId, HttpHeaders headers, Authentication auth) throws IOException {
        String email = auth.getName();
        users student = UserRepo.findByEmail(email).orElseThrow(() ->
                new RuntimeException("User not found"));

        courseVideo video = VideoRepo.findByVideoId(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        Courses course = CourseRepo.findByCourseCode(video.getCourseCode())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        String courseCode = video.getCourseCode();
        boolean isInstructor = course.getInstructorCode().equals(student.getCode());
        boolean isEnrolled = EnrollRepo.existsByStudentIdAndCourseCode(student, courseCode);

        if (!isEnrolled && !isInstructor) {
            throw new RuntimeException("Access Denied: You are not enrolled in this course");
        }

        String projectRoot = System.getProperty("user.dir");
        Path path = Paths.get(projectRoot, video.getVideoPath());

        if (!Files.exists(path)) {
            System.err.println("CRITICAL: Video not found at " + path.toAbsolutePath());
            throw new RuntimeException("Video file not found on server");
        }

        UrlResource videoResource = new UrlResource(path.toUri());
        long contentLength = videoResource.contentLength();

        HttpRange range = headers.getRange().isEmpty() ? null : headers.getRange().get(0);

        if (range != null) {
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(1024 * 1024, end - start + 1); // 1MB chunks
            return new ResourceRegion(videoResource, start, rangeLength);
        } else {
            long rangeLength = Math.min(1024 * 1024, contentLength);
            return new ResourceRegion(videoResource, 0, rangeLength);
        }
    }
    public ApiResponse<videoResponse> deleteVideo(String videoId) {
        courseVideo video = VideoRepo.findByVideoId(videoId).orElseThrow(()->new RuntimeException("Video not found"));
        VideoRepo.delete(video);
        try {
            if (video.getThumbnailPath() != null) {
                Files.deleteIfExists(Paths.get(video.getThumbnailPath()));
            }
            if (video.getVideoPath() != null) {
                Files.deleteIfExists(Paths.get(video.getVideoPath()));
            }
        }catch (IOException e){
            throw new RuntimeException("Error deleting video files",e);
        }
    Courses course = CourseRepo.findByCourseCode(video.getCourseCode()).orElseThrow(() -> new RuntimeException("Course not found"));
        if (course != null && video.getDuration() != null) {
            double newHours =course.getCourseHours() - (video.getDuration()/60.0);
            int videoCounter = course.getVideoCounter() -1;
            int thumbnailCounter = course.getThumbnailCounter() -1;
            course.setCourseHours(Math.max(newHours,0.0));
            course.setVideoCounter(videoCounter);
            course.setThumbnailCounter(thumbnailCounter);
            CourseRepo.save(course);
        }
        return new ApiResponse<>("video deleted successfully", null);
    }
    public ApiResponse<videoResponse> getVideo(String videoId) {
        courseVideo video = VideoRepo.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        videoResponse response = videoResponse.builder()
                .videoId(video.getVideoId())
                .courseCode(video.getCourseCode())
                .Title(video.getTitle())
                .Description(video.getDescription())
                .videoPath(video.getVideoPath())
                .thumbnailPath(video.getThumbnailPath())
                .duration(video.getDuration())
                .build();

        return new ApiResponse<>("video fetched successfully", response);
    }
    public ApiResponse<List<videoResponse>> getVideosByCourse(String courseCode) {
        List<courseVideo> videos = VideoRepo.findByCourseCode(courseCode);

        List<videoResponse> responses = videos.stream()
                .map(video -> videoResponse.builder()
                        .videoId(video.getVideoId())
                        .courseCode(video.getCourseCode())
                        .Title(video.getTitle())
                        .Description(video.getDescription())
                        .videoPath(video.getVideoPath())
                        .thumbnailPath(video.getThumbnailPath())
                        .duration(video.getDuration())
                        .build())
                .toList();

        return new ApiResponse<>("course videos fetched successfully", responses);
    }

}
