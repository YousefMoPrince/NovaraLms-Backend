package com.amarjo.novaralms.chat.dto;

import com.amarjo.novaralms.chat.model.MessageType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageDto {
    @NotBlank(message = "Content cannot be empty")
    private String content;
    private String roomId;
    private MessageType type = MessageType.TEXT;
    private String mediaUrl;
}