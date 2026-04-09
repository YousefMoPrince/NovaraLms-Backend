package com.amarjo.novaralms.course.DTO;

import lombok.Data;

@Data
public class videoRequest {
    private String courseCode;
    private String Title;
    private String Description;
    private String videoPath;
    private String thumbnailPath;
    private Integer duration;
}
