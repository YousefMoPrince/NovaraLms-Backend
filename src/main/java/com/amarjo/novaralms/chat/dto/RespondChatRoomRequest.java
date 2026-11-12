package com.amarjo.novaralms.chat.dto;

import com.amarjo.novaralms.chat.model.ChatRoomStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RespondChatRoomRequest {
    @NotNull(message = "Status action is required")
    private ChatRoomStatus status;
    private String roomId;
}