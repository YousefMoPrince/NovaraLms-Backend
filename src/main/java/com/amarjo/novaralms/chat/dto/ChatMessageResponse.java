package com.amarjo.novaralms.chat.dto;

import com.amarjo.novaralms.chat.model.MessageStatus;
import com.amarjo.novaralms.chat.model.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {
    private String id;
    private String roomId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private MessageType type;
    private String mediaUrl;
    private MessageStatus status;
    private LocalDateTime timestamp;
}