package com.amarjo.novaralms.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateChatRoomRequest {
    @NotBlank(message = "Course code is required")
    private String courseCode;
}