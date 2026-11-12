package com.amarjo.novaralms.course.Services;

import com.amarjo.novaralms.auth.Services.SessionService;
import com.amarjo.novaralms.auth.model.users;
import com.amarjo.novaralms.auth.repository.UserRepo;
import com.amarjo.novaralms.course.DTO.CourseApiResponse;
import com.amarjo.novaralms.course.DTO.VideoRequest;
import com.amarjo.novaralms.course.DTO.VideoResponse;
import com.amarjo.novaralms.course.model.Courses;
import com.amarjo.novaralms.course.model.CourseVideo;
import com.amarjo.novaralms.course.model.EnrollmentStatus;
import com.amarjo.novaralms.course.model.VideoStatus;
import com.amarjo.novaralms.course.repo.CourseRepo;
import com.amarjo.novaralms.course.repo.EnrollmentRepo;
import com.amarjo.novaralms.course.repo.VideoRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class VideoServices {
    private final VideoRepo videoRepo;

    private final CourseRepo courseRepo;

    private final UserRepo UserRepo;

    private final EnrollmentRepo enrollmentRepo;

    private final SessionService sessionService;

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

    public CourseApiResponse<VideoResponse> addVideo(VideoRequest request, Character c) {
        try {
            String email = userAuth(c);
            users user = UserRepo.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"user not found"));
            Courses course = courseRepo.findByCourseCode(request.getCourseCode())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"course not found"));
            if (!user.getCode().equals(course.getInstructorCode()))
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You do not own this course");
            }
            CourseVideo video = CourseVideo.builder()
                    .courseCode(request.getCourseCode())
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .durationInSeconds(request.getDurationInSeconds())
                    .orderIndex(request.getOrderIndex())
                    .status(VideoStatus.DRAFT)
                    .build();
            videoRepo.save(video);
            VideoResponse response = VideoResponse.builder().videoId(video.getVideoId()).build();
            return new CourseApiResponse<>("Video added", response);
        } catch (ResponseStatusException e) {
            throw e;
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add video : " + e.getMessage(), e);
        }
    }
    public CourseApiResponse<String> uploadVideoThumbnail(String courseCode, String videoId, MultipartFile file,Character c) throws IOException {
        try {
            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Video file is required and cannot be empty");
            }
            String email = userAuth(c);
            users user = UserRepo.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"user not found"));
            Courses course = courseRepo.findByCourseCode(courseCode)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Course not found"));
            CourseVideo video = videoRepo.findByVideoId(videoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Video not found"));
            if (!user.getCode().equals(course.getInstructorCode())){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You do not own this course");
            }

            int index = course.getThumbnailCounter() + 1;
            String fileName = courseCode + "_" + videoId + "_" + index + ".jpg";

            String projectRoot = System.getProperty("user.dir");
            Path uploadPath = Paths.get(projectRoot, "storage", "Thumbnail", "videoThumbnail", courseCode);

            if (Files.notExists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path destinationFile = uploadPath.resolve(fileName);

            try {
                file.transferTo(destinationFile.toFile());
            } catch (IOException e) {
                throw new IOException("Critical error: Could not save video thumbnail. " + e.getMessage());
            }

            String dbPath = "storage/Thumbnail/videoThumbnail/" + courseCode + "/" + fileName;

            course.setThumbnailCounter(index);
            courseRepo.save(course);

            video.setThumbnailPath(dbPath);
            videoRepo.save(video);

            return new CourseApiResponse<>("Video thumbnail uploaded successfully", dbPath);
        } catch (ResponseStatusException e) {
            throw e;
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error while uploading video thumbnail : " + e.getMessage(),e);
        }
    }
    public CourseApiResponse<String> uploadVideo(String courseCode, String videoId, MultipartFile file,Character c) throws IOException {

            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Video file is required and cannot be empty");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file format. Only video files are allowed");
            }
            String email = userAuth(c);
            users user = UserRepo.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"user not found"));
            Courses course = courseRepo.findByCourseCode(courseCode)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Course not found"));
            CourseVideo video = videoRepo.findByVideoId(videoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Video not found"));
            if (!user.getCode().equals(course.getInstructorCode())){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You do not own this course");
            }
        try {
            int index = course.getVideoCounter() + 1;
            String fileName = courseCode + "_" + index + "_" + videoId + ".mp4";

            String projectRoot = System.getProperty("user.dir");
            Path uploadPath = Paths.get(projectRoot, "storage", "courseVideos", courseCode);

            if (Files.notExists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path destinationPath = uploadPath.resolve(fileName);

            file.transferTo(destinationPath.toFile());

            course.setVideoCounter(index);
            courseRepo.save(course);

            String dbPath = "storage/courseVideos/" + courseCode + "/" + fileName;
            video.setVideoPath(dbPath);
            video.setStatus(VideoStatus.READY);
            videoRepo.save(video);

            return new CourseApiResponse<>("Video uploaded successfully", dbPath);
        } catch (ResponseStatusException e) {
            throw e;
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload video file: " + e.getMessage(), e);
        }
    }
    public CourseApiResponse<VideoResponse> finalizeVideo(String videoId, Character c){
        try {
            String email = userAuth(c);
            users user = UserRepo.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"user not found"));
            CourseVideo video = videoRepo.findByVideoId(videoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Video not found"));
            Courses course = courseRepo.findByCourseCode(video.getCourseCode())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Course not found"));
            if (!user.getCode().equals(course.getInstructorCode())){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You are not the instructor of this video");
            }
            if (video.getStatus() != VideoStatus.PUBLISHED) {
                video.setStatus(VideoStatus.PUBLISHED);
                videoRepo.save(video);
                double currentHours = (course.getCourseHours() != null) ? course.getCourseHours().doubleValue() : 0.0;
                double addedHours = (video.getDurationInSeconds() != null) ? (video.getDurationInSeconds() / 3600.0) : 0.0;
                course.setCourseHours(BigDecimal.valueOf(currentHours + addedHours));
                courseRepo.save(course);
            }
            VideoResponse response = VideoResponse.builder()
                    .videoId(video.getVideoId())
                    .courseCode(video.getCourseCode())
                    .title(video.getTitle())
                    .description(video.getDescription())
                    .videoPath(video.getVideoPath())
                    .thumbnailPath(video.getThumbnailPath())
                    .durationInSeconds(video.getDurationInSeconds())
                    .orderIndex(video.getOrderIndex())
                    .createdAt(video.getCreatedAt())
                    .build();

            return new CourseApiResponse<>("video finalized successfully", response);
        } catch (ResponseStatusException e) {
            throw e;
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to Publish video : " + e.getMessage(), e);
        }
    }
    public ResourceRegion streamVideo(String videoId, HttpHeaders headers, Character c) throws IOException {
        try {
            String email = userAuth(c);
            users user = UserRepo.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            CourseVideo video = videoRepo.findByVideoId(videoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found"));
            Courses course = courseRepo.findByCourseCode(video.getCourseCode())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

            boolean isInstructor = user.getCode().equals(course.getInstructorCode());
            boolean isEnrolled = enrollmentRepo.existsByStudentAndCourseCodeAndEnrollmentStatus(
                    user, course.getCourseCode(), EnrollmentStatus.ACTIVE
            );

            if (!isInstructor && !isEnrolled) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You are not have this course");
            }

            if (video.getVideoPath() == null || video.getVideoPath().isBlank()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video file path is not set");
            }

            String projectRoot = System.getProperty("user.dir");
            Path path = Paths.get(projectRoot, video.getVideoPath());

            if (!Files.exists(path)) {
                System.err.println("CRITICAL: Video file missing at path: " + path.toAbsolutePath());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video file does not exist on server");
            }

            UrlResource videoResource = new UrlResource(path.toUri());
            long contentLength = videoResource.contentLength();

            long chunkSize = 1024 * 1024;

            HttpRange range = headers.getRange().isEmpty() ? null : headers.getRange().getFirst();

            if (range != null) {
                long start = range.getRangeStart(contentLength);
                long end = range.getRangeEnd(contentLength);
                long rangeLength = Math.min(chunkSize, end - start + 1);
                return new ResourceRegion(videoResource, start, rangeLength);
            } else {
                long rangeLength = Math.min(chunkSize, contentLength);
                return new ResourceRegion(videoResource, 0, rangeLength);
            }
        }catch (ResponseStatusException e) {
            throw e;
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error while streaming video : " + e.getMessage(),e);
        }
    }
    public CourseApiResponse<VideoResponse> deleteVideo(String videoId, Character c) {
        try {
            String email = userAuth(c);
            users user = UserRepo.findByEmail(email)
                    .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
            CourseVideo video = videoRepo.findByVideoId(videoId)
                    .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"video not found"));
            Courses course = courseRepo.findByCourseCode(video.getCourseCode())
                    .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"course not found"));
            if (!user.getCode().equals(course.getInstructorCode())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You are not the instructor of this video");
            }
            if (video.getStatus() == VideoStatus.PUBLISHED && video.getDurationInSeconds() != null) {
                double currentHours = (course.getCourseHours() != null) ? course.getCourseHours().doubleValue() : 0.0;
                double removedHours = video.getDurationInSeconds() / 3600.0;
                course.setCourseHours(BigDecimal.valueOf(Math.max(0.0, currentHours - removedHours)));
                courseRepo.save(course);
            }
                try {
                    String projectRoot = System.getProperty("user.dir");
                    if (video.getThumbnailPath() != null) {
                        Files.deleteIfExists(Paths.get(projectRoot, video.getThumbnailPath()));
                    }
                    if (video.getVideoPath() != null) {
                        Files.deleteIfExists(Paths.get(projectRoot, video.getVideoPath()));
                    }
                } catch (IOException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error deleting video files", e);
                }
                courseRepo.save(course);
                videoRepo.delete(video);
            return new CourseApiResponse<>("video deleted successfully", null);
        } catch (ResponseStatusException e) {
            throw e;
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error while deleting video : " + e.getMessage(),e);
        }
    }
    public CourseApiResponse<VideoResponse> getVideo(String videoId, Character c) {
        try {
            String email = userAuth(c);
            users user = UserRepo.findByEmail(email).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
            CourseVideo video = videoRepo.findByVideoId(videoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Video not found"));

            Courses course = courseRepo.findByCourseCode(video.getCourseCode())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Course not found"));

            boolean isEnrolled = enrollmentRepo.existsByStudentAndCourseCodeAndEnrollmentStatus(user, course.getCourseCode(), EnrollmentStatus.ACTIVE);
            boolean isInstructor = user.getCode().equals(course.getInstructorCode());
            if (!isEnrolled && !isInstructor) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Access Denied: You are not have this course");
            }
            VideoResponse response = VideoResponse.builder()
                    .videoId(video.getVideoId())
                    .courseCode(video.getCourseCode())
                    .title(video.getTitle())
                    .description(video.getDescription())
                    .videoPath(video.getVideoPath())
                    .thumbnailPath(video.getThumbnailPath())
                    .durationInSeconds(video.getDurationInSeconds())
                    .createdAt(video.getCreatedAt())
                    .updatedAt(video.getUpdatedAt())
                    .orderIndex(video.getOrderIndex())
                    .build();
            return new CourseApiResponse<>("video fetched successfully", response);
        }catch (ResponseStatusException e) {
            throw e;
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error while retrieving video : " + e.getMessage(),e);
        }
    }
    public CourseApiResponse<List<VideoResponse>> getVideosByCourse(String courseCode, Character c) {
        try {
            String email = userAuth(c);
            users user = UserRepo.findByEmail(email).orElseThrow(() ->
                    new RuntimeException("User not found"));
            Courses course = courseRepo.findByCourseCode(courseCode)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Course not found"));

            boolean isEnrolled = enrollmentRepo.existsByStudentAndCourseCodeAndEnrollmentStatus(user, course.getCourseCode(), EnrollmentStatus.ACTIVE);
            boolean isInstructor = user.getCode().equals(course.getInstructorCode());
            if (!isEnrolled && !isInstructor) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Access Denied: You are not have this course");
            }
            List<CourseVideo> videos = videoRepo.findByCourseCode(courseCode);
            List<VideoResponse> responses = videos.stream()
                    .map(video -> VideoResponse.builder()
                            .videoId(video.getVideoId())
                            .courseCode(video.getCourseCode())
                            .title(video.getTitle())
                            .description(video.getDescription())
                            .videoPath(video.getVideoPath())
                            .thumbnailPath(video.getThumbnailPath())
                            .durationInSeconds(video.getDurationInSeconds())
                            .build())
                    .toList();

            return new CourseApiResponse<>("course videos fetched successfully", responses);
        }catch (ResponseStatusException e) {
            throw e;
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error while fetching videos : " + e.getMessage(),e);
        }
    }

}
