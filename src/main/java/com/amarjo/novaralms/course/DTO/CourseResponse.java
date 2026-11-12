package com.amarjo.novaralms.course.DTO;

import com.amarjo.novaralms.course.model.CourseStatus;
import com.amarjo.novaralms.course.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CourseResponse {
    private String courseCode;
    private String courseName;
    private String department;
    private BigDecimal price;
    private Currency currency;
    private String shortDescription;
    private String courseDescription;
    private String thumbnailPath;
    private BigDecimal courseHours;
    private String createdAt;
    private String updatedAt;
    private String createdBy;
    private String instructorCode;
    private CourseStatus status;
    private Integer videoCounter;
}
