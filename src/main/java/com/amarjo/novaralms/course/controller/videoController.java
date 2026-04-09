package com.amarjo.novaralms.course.controller;

import com.amarjo.novaralms.course.DTO.ApiResponse;
import com.amarjo.novaralms.course.DTO.videoRequest;
import com.amarjo.novaralms.course.DTO.videoResponse;
import com.amarjo.novaralms.course.Services.videoServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @PostMapping("/{courseCode}/uploadvideothumbnail")
    public ResponseEntity<ApiResponse<String>> uploadVideoThumbnail(@PathVariable String courseCode, @RequestParam("file") MultipartFile file)
            throws IOException {
        return new ResponseEntity<>(VideoServices.uploadVideoThumbnail(courseCode,file),HttpStatus.OK);
    }
    @PostMapping("/{courseCode}/uploadvideo")
    public ResponseEntity<ApiResponse<String>> uploadVideo(@PathVariable String courseCode, @RequestParam("file") MultipartFile file)
            throws IOException {
        return new ResponseEntity<>(VideoServices.uploadVideo(courseCode,file),HttpStatus.OK);
    }
    @PutMapping("/{videoId}/finalizevideo")
    public ResponseEntity<ApiResponse<videoResponse>> finalizeVideo(@PathVariable String videoId, @RequestBody videoRequest VideoRequest)
    {
        return new ResponseEntity<>(VideoServices.finalizeVideo(videoId,VideoRequest),HttpStatus.OK);
    }
    @DeleteMapping("/{videoId}/deletevideo")
    public ResponseEntity<ApiResponse<videoResponse>> deleteVideo(@PathVariable String videoId){
        return new ResponseEntity<>(VideoServices.deleteVideo(videoId),HttpStatus.OK);
    }
    @GetMapping("/{videoId}/getvideo")
    public ResponseEntity<ApiResponse<videoResponse>> getVideo(@PathVariable String videoId){
        return new ResponseEntity<>(VideoServices.getVideo(videoId),HttpStatus.OK);
    }
    @GetMapping("/{videoId}/allvideos")
    public ResponseEntity<ApiResponse<List<videoResponse>>> getVideosByCourse(@PathVariable String videoId){
        return new ResponseEntity<>(VideoServices.getVideosByCourse(videoId),HttpStatus.OK);
    }
}
