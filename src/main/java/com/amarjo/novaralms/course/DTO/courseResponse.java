package com.amarjo.novaralms.course.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class courseResponse {
    private String courseCode;
    private String courseName;
    private String department;
    private String shortDescription;
    private String courseDescription;
    private String thumbnailPath;
    private Integer courseHours;
    private String createdAt;
    private Long instructorCode;
}
