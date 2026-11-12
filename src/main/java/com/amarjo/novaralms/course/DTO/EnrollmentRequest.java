package com.amarjo.novaralms.course.DTO;

import lombok.Data;

@Data
public class EnrollmentRequest {
    private String courseCode;
    private String instructorCode;
}
