package com.amarjo.novaralms.chat.controller;

import com.amarjo.novaralms.chat.dto.*;
import com.amarjo.novaralms.chat.services.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/web/v1/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    private final static Character c = 'W';

    @PostMapping("/rooms/request")
    public ResponseEntity<ChatRoomResponse> requestChatRoom(
            @Valid @RequestBody CreateChatRoomRequest request) {
        return ResponseEntity.ok(chatService.requestChatRoom(c, request));
    }

    @PatchMapping("/rooms/respond")
    public ResponseEntity<ChatRoomResponse> respondToChatRequest(
            @Valid @RequestBody RespondChatRoomRequest response) {
        return ResponseEntity.ok(chatService.respondToChatRequest(c, response));
    }

    @PostMapping("/messages/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @Valid @RequestBody SendMessageDto messageDto) {
        return ResponseEntity.ok(chatService.sendMessage(messageDto, c));
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getUserChatRooms() {
        return ResponseEntity.ok(chatService.getUserChatRooms(c));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Slice<ChatMessageResponse>> getRoomMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(chatService.getRoomMessages(c, roomId, page, size));
    }
}