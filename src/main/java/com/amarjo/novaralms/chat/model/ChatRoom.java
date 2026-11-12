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
@Document(collection = "chat_rooms")
@CompoundIndex(name = "student_course_inst_idx", def = "{'studentId': 1, 'courseCode': 1, 'instructorId': 1}", unique = true)
public class ChatRoom {
    @Id
    private String id;
    private String courseCode;
    private Long studentId;
    private Long instructorId;
    private ChatRoomStatus status;
    private String requestedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LastMessage lastMessage;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LastMessage {
        private Long senderId;
        private String content;
        private LocalDateTime timestamp;
    }
}