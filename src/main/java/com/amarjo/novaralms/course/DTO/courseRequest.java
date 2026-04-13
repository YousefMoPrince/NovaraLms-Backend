package com.amarjo.novaralms.course.DTO;

import lombok.Data;

@Data
public class courseRequest {
    private String courseName;
    private String department;
    private String shortDescription;
    private String courseDescription;
    private String thumbnailPath;
    private Double courseHours;
    private String createdAt;
    private String instructorCode;
}
