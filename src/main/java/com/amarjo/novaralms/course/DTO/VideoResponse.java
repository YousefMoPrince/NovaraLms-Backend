package com.amarjo.novaralms.course.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoResponse {
    private String videoId;
    private String courseCode;
    private String videoPath;
    private String thumbnailPath;
    private String title;
    private String description;
    private Integer durationInSeconds;
    private Integer orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
