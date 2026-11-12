package com.amarjo.novaralms.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "chat_messages")
@CompoundIndex(name = "room_time_idx", def = "{'roomId': 1, 'timestamp': -1}")
public class ChatMessage {
    @Id
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