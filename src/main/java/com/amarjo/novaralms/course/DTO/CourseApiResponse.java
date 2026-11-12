package com.amarjo.novaralms.course.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseApiResponse<T> {
    private String message;
    private T data;
}
