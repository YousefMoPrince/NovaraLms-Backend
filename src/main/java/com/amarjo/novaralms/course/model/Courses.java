package com.amarjo.novaralms.course.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Document(collection = "Courses")
public class Courses {
    @Id
    private String courseId;
    private String courseCode;
    private String courseName;
    private String department;
    private String shortDescription;
    private String courseDescription;
    private String thumbnailPath;
    private Integer courseHours;
    private LocalDateTime createdAt;
    private Long instructorCode;
    private Integer videoCounter = 0;
    private Integer thumbnailCounter = 0;


}
