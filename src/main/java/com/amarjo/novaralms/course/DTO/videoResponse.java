package com.amarjo.novaralms.course.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class videoResponse {
    private String videoId;
    private String courseCode;
    private String Title;
    private String Description;
    private String videoPath;
    private String thumbnailPath;
    private Integer duration;
}
