package com.amarjo.novaralms.notification.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    private String id;
    @Indexed
    private Long recipientUserId; // المستلم
    private String title;
    private String body;
    private String imageUrl;
    private String type;
    private Map<String, String> data;
    @Builder.Default
    private boolean isRead = false;
    @Indexed
    private LocalDateTime createdAt;
}