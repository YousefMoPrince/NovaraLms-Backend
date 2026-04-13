package com.amarjo.novaralms.course.DTO;

import lombok.Data;

@Data
public class videoRequest {
    private String courseCode;
    private String title;
    private String description;
    private String videoPath;
    private String thumbnailPath;
    private Integer duration;
}
