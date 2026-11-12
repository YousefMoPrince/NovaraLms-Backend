package com.amarjo.novaralms.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private String id;
    private String title;
    private String body;
    private String imageUrl;
    private String type;
    private Map<String, String> data;
    private boolean isRead;
    private LocalDateTime createdAt;
}