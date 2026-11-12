package com.amarjo.novaralms.notification.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "user_device_tokens")
@CompoundIndex(name = "user_device_unique_idx", def = "{'userId': 1, 'deviceToken': 1}", unique = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDeviceToken {
    @Id
    private String id;
    @Indexed
    private Long userId;
    @Indexed
    private String deviceToken;
    private String deviceType;
    private LocalDateTime updatedAt;
}