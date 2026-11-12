package com.amarjo.novaralms.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class TopicNotificationRequest {

    @NotBlank(message = "Topic name is required")
    private String topic;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body is required")
    private String body;

    private Map<String, String> data;
}