package com.amarjo.novaralms.chat.dto;

import com.amarjo.novaralms.chat.model.ChatRoomStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatRoomResponse {
    private String id;
    private String courseCode;
    private Long studentId;
    private Long instructorId;
    private ChatRoomStatus status;
    private ChatMessageResponse lastMessage;
    private long unreadCount;
    private LocalDateTime updatedAt;
}