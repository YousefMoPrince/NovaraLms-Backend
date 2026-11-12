package com.amarjo.novaralms.course.DTO;

import com.amarjo.novaralms.course.model.EnrollmentStatus;
import com.amarjo.novaralms.course.model.StudentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
public class EnrollmentResponse {
    private Long enrollmentId;
    private String courseCode;
    private String instructorCode;
    private EnrollmentStatus enrollmentStatus;
    private StudentStatus studentStatus;
    private int contentFinished;
    private LocalDateTime enrollmentDate;
    private Long studentId;
}
