package com.amarjo.novaralms.chat.controller;

import com.amarjo.novaralms.chat.dto.SendMessageDto;
import com.amarjo.novaralms.chat.services.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final ChatService chatService;
    private final static Character c = 'W';
    @MessageMapping("/web/chat.sendMessage")
    public void sendMessage(
            @Payload SendMessageDto messageDto) {
        log.info("Received WebSocket message for room: {} from clientType: {}", messageDto.getRoomId(), c);
        chatService.sendMessage(messageDto, c);
    }

}