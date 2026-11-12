package com.amarjo.novaralms.course.controller;

import com.amarjo.novaralms.course.DTO.CourseApiResponse;
import com.amarjo.novaralms.course.DTO.VideoRequest;
import com.amarjo.novaralms.course.DTO.VideoResponse;
import com.amarjo.novaralms.course.Services.VideoServices;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/mob/v1/video")
@RequiredArgsConstructor
public class MobileVideoController {

    private final VideoServices videoServices;
    private static final Character PLATFORM = 'M';

    @PostMapping("/add")
    public ResponseEntity<CourseApiResponse<VideoResponse>> addVideo(@RequestBody VideoRequest request) {
        return new ResponseEntity<>(videoServices.addVideo(request, PLATFORM), HttpStatus.CREATED);
    }

    @PostMapping("/{courseCode}/upload/thumbnail/{videoId}")
    public ResponseEntity<CourseApiResponse<String>> uploadVideoThumbnail(
            @PathVariable String courseCode,
            @PathVariable String videoId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(videoServices.uploadVideoThumbnail(courseCode, videoId, file, PLATFORM));
    }

    @PostMapping("/{courseCode}/upload/video/{videoId}")
    public ResponseEntity<CourseApiResponse<String>> uploadVideo(
            @PathVariable String courseCode,
            @PathVariable String videoId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(videoServices.uploadVideo(courseCode, videoId, file, PLATFORM));
    }

    @PatchMapping("/{videoId}/finalize")
    public ResponseEntity<CourseApiResponse<VideoResponse>> finalizeVideo(@PathVariable String videoId) {
        return ResponseEntity.ok(videoServices.finalizeVideo(videoId, PLATFORM));
    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<ResourceRegion> streamVideo(
            @PathVariable String videoId,
            @RequestHeader HttpHeaders headers) throws IOException {
        ResourceRegion video = videoServices.streamVideo(videoId, headers, PLATFORM);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.parseMediaType("video/mp4"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(video);
    }

    @DeleteMapping("/{videoId}/delete")
    public ResponseEntity<CourseApiResponse<VideoResponse>> deleteVideo(@PathVariable String videoId) {
        return ResponseEntity.ok(videoServices.deleteVideo(videoId, PLATFORM));
    }

    @GetMapping("/{videoId}/get")
    public ResponseEntity<CourseApiResponse<VideoResponse>> getVideo(@PathVariable String videoId) {
        return ResponseEntity.ok(videoServices.getVideo(videoId, PLATFORM));
    }

    @GetMapping("/{courseCode}/all")
    public ResponseEntity<CourseApiResponse<List<VideoResponse>>> getVideosByCourse(@PathVariable String courseCode) {
        return ResponseEntity.ok(videoServices.getVideosByCourse(courseCode, PLATFORM));
    }
}