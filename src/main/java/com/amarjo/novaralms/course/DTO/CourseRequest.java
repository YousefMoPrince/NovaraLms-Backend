package com.amarjo.novaralms.course.DTO;

import com.amarjo.novaralms.course.model.Currency;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseRequest {
    private String courseName;
    private String department;
    private BigDecimal price;
    private Currency currency;
    private String shortDescription;
    private String courseDescription;
}
