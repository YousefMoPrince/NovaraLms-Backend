package com.amarjo.novaralms.course.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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
    @Indexed(unique = true)
    private String courseCode;
    private String courseName;
    private String department;
    private String shortDescription;
    private String courseDescription;
    private String thumbnailPath;
    private Double courseHours;
    private LocalDateTime createdAt;
    private String instructorCode;
    private Integer videoCounter = 0;
    private Integer thumbnailCounter = 0;


}
