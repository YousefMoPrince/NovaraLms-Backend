package com.amarjo.novaralms.course.DTO;

import lombok.Data;

@Data
public class VideoRequest {
    private String courseCode;
    private String title;
    private String description;
    private String videoPath;
    private Integer durationInSeconds;
    private Integer orderIndex;
}
