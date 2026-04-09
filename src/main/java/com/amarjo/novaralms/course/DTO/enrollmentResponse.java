package com.amarjo.novaralms.course.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class enrollmentResponse {
    private Long enrollmentId;
    private String courseCode;
    private String instructorCode;
    private Date enrollmentDate;
    private Long studentId;
}
