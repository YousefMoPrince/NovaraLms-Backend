package com.amarjo.novaralms.course.Services;

import com.amarjo.novaralms.course.DTO.ApiResponse;
import com.amarjo.novaralms.course.DTO.videoRequest;
import com.amarjo.novaralms.course.DTO.videoResponse;
import com.amarjo.novaralms.course.model.Courses;
import com.amarjo.novaralms.course.model.courseVideo;
import com.amarjo.novaralms.course.repo.courseRepo;
import com.amarjo.novaralms.course.repo.videoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class videoServices {
    @Autowired
    private videoRepo VideoRepo;
    @Autowired
    private courseRepo CourseRepo;

    public ApiResponse<videoResponse> addVideo(videoRequest request) {
        courseVideo video = courseVideo.builder().courseCode(request.getCourseCode()).Title(request.getTitle()).build();
        VideoRepo.save(video);
        videoResponse response = videoResponse.builder().videoId(video.getVideoId()).build();
        return new ApiResponse<>("Video added", response);
    }
    public ApiResponse<String> uploadVideoThumbnail(String courseCode, MultipartFile file) throws IOException {
        Courses course = CourseRepo.findByCourseCode(courseCode).orElseThrow(() -> new RuntimeException("Course not found"));
        if (course == null) {
            throw new RuntimeException("Course not found");
        }
        int index = course.getThumbnailCounter() + 1;
        course.setThumbnailCounter(index);
        CourseRepo.save(course);
        String fileName = courseCode + "_" + index + ".jpg";
        Path path = Paths.get("storage/Thumbnail/videoThumbnail/"+courseCode+"/"+fileName);
        Files.createDirectories(path.getParent());
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        return new ApiResponse<>("video thumbnail uploaded", path.toString());
    }
    public ApiResponse<String> uploadVideo(String courseCode, MultipartFile file) throws IOException {
        Courses course = CourseRepo.findByCourseCode(courseCode).orElseThrow(() -> new RuntimeException("Course not found"));
        if (course == null) {
            throw new RuntimeException("Course not found");
        }
        int index = course.getVideoCounter() + 1;
        course.setVideoCounter(index);
        CourseRepo.save(course);
        String fileName = courseCode + "_" + index + ".mp4";
        Path path = Paths.get("storage/courseVideos/" + courseCode + "/" + fileName);
        Files.createDirectories(path.getParent());
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        return new ApiResponse<>("video uploaded", path.toString());
    }
    public ApiResponse<videoResponse> finalizeVideo(String videoId, videoRequest request) {
        courseVideo video = VideoRepo.findByVideoId(videoId);
        video.setDescription(request.getDescription());
        video.setVideoPath(request.getVideoPath());
        video.setThumbnailPath(request.getThumbnailPath());
        video.setDuration(request.getDuration());
        VideoRepo.save(video);

        Courses course = CourseRepo.findByCourseCode(video.getCourseCode()).orElseThrow(() -> new RuntimeException("Course not found"));
        if (course != null) {
            course.setCourseHours(course.getCourseHours() + (video.getDuration()/60));
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
    public ApiResponse<videoResponse> deleteVideo(String videoId) {
        courseVideo video = VideoRepo.findByVideoId(videoId);
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
            int newHours =course.getCourseHours() - (video.getDuration()/60);
            course.setCourseHours(Math.max(newHours,0));
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
