package com.amarjo.novaralms.course.controller;

import com.amarjo.novaralms.course.DTO.ApiResponse;
import com.amarjo.novaralms.course.DTO.videoRequest;
import com.amarjo.novaralms.course.DTO.videoResponse;
import com.amarjo.novaralms.course.Services.videoServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/video")
public class videoController {
    @Autowired
    private videoServices VideoServices;

    @PostMapping("/addvideo")
    public ResponseEntity<ApiResponse<videoResponse>> addVideo(@RequestBody videoRequest VideoRequest) {
        return new ResponseEntity<>(VideoServices.addVideo(VideoRequest), HttpStatus.CREATED);
    }
    @PostMapping("/{courseCode}/uploadvideothumbnail/{videoId}")
    public ResponseEntity<ApiResponse<String>> uploadVideoThumbnail(@PathVariable String courseCode,@PathVariable String videoId, @RequestParam("file") MultipartFile file)
            throws IOException {
        return new ResponseEntity<>(VideoServices.uploadVideoThumbnail(courseCode,videoId,file),HttpStatus.OK);
    }
    @PostMapping("/{courseCode}/uploadvideo/{videoId}")
    public ResponseEntity<ApiResponse<String>> uploadVideo(@PathVariable String courseCode,@PathVariable String videoId, @RequestParam("file") MultipartFile file)
            throws IOException {
        return new ResponseEntity<>(VideoServices.uploadVideo(courseCode,videoId,file),HttpStatus.OK);
    }
    @PatchMapping("/{videoId}/finalizevideo")
    public ResponseEntity<ApiResponse<videoResponse>> finalizeVideo(@PathVariable String videoId, @RequestBody videoRequest VideoRequest)
    {
        return new ResponseEntity<>(VideoServices.finalizeVideo(videoId,VideoRequest),HttpStatus.OK);
    }
    @GetMapping("/stream/{videoId}")
    public ResponseEntity<ResourceRegion> streamVideo(@PathVariable String videoId, @RequestHeader HttpHeaders headers, Authentication auth) throws IOException {
       try {


           ResourceRegion video = VideoServices.streamVideo(videoId, headers, auth);
           return ResponseEntity.ok()
                   .contentType(MediaType.parseMediaType("video/mp4"))
                   .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                   .body(video);
       }catch (RuntimeException e){
           return new ResponseEntity<>(HttpStatus.FORBIDDEN);
       }catch (IOException e){
           return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }
    @DeleteMapping("/{videoId}/deletevideo")
    public ResponseEntity<ApiResponse<videoResponse>> deleteVideo(@PathVariable String videoId){
        return new ResponseEntity<>(VideoServices.deleteVideo(videoId),HttpStatus.OK);
    }
    @GetMapping("/{videoId}/getvideo")
    public ResponseEntity<ApiResponse<videoResponse>> getVideo(@PathVariable String videoId){
        return new ResponseEntity<>(VideoServices.getVideo(videoId),HttpStatus.OK);
    }
    @GetMapping("/{courseCode}/allvideos")
    public ResponseEntity<ApiResponse<List<videoResponse>>> getVideosByCourse(@PathVariable String courseCode){
        return new ResponseEntity<>(VideoServices.getVideosByCourse(courseCode),HttpStatus.OK);
    }
}
