package com.amarjo.novaralms.course.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Document(collection = "courses")
public class Courses {
    @Id
    private String courseId;
    @Indexed(unique = true)
    private String courseCode;
    private String courseName;
    private String department;
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;
    private Currency currency;
    private String shortDescription;
    private String courseDescription;
    private String thumbnailPath;
    private BigDecimal courseHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String instructorCode;
    private CourseStatus status;
    @Builder.Default
    private Integer videoCounter = 0;
    @Builder.Default
    private Integer thumbnailCounter = 0;
}
