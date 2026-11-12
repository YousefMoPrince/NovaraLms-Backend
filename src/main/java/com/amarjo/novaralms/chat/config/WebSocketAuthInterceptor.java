package com.amarjo.novaralms.chat.config;

import com.amarjo.novaralms.auth.Services.JwtService;
import com.amarjo.novaralms.chat.model.ChatRoom;
import com.amarjo.novaralms.chat.model.ChatRoomStatus;
import com.amarjo.novaralms.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {

                Authentication sessionAuth = (Authentication) accessor.getUser();
                if (sessionAuth != null && sessionAuth.isAuthenticated()) {
                    log.info("WebSocket Authenticated via HTTP Session for User: {}", sessionAuth.getName());
                    return message;
                }

                String authHeader = accessor.getFirstNativeHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String jwt = authHeader.substring(7);

                    try {
                        String username = jwtService.extractUsername(jwt);

                        if (username != null && jwtService.isTokenValid(jwt, username)) {
                            String role = jwtService.extractRole(jwt);
                            List<SimpleGrantedAuthority> authorities = (role != null)
                                    ? List.of(new SimpleGrantedAuthority(role))
                                    : List.of();

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(username, null, authorities);

                            accessor.setUser(authentication);
                            log.info("WebSocket Authenticated via JWT Token for User: {}", username);
                        } else {
                            log.warn("WebSocket Auth Failed: Invalid or Expired JWT");
                            throw new IllegalArgumentException("Invalid or Expired JWT Token");
                        }
                    } catch (Exception ex) {
                        log.error("WebSocket JWT Auth Error: {}", ex.getMessage());
                        throw new IllegalArgumentException("Unauthorized WebSocket Connection");
                    }
                } else {
                    log.warn("WebSocket Auth Failed: Neither Session nor JWT Header present");
                    throw new IllegalArgumentException("Authentication required (Session Cookie or JWT Header)");
                }
            }

            else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand())) {
                String destination = accessor.getDestination();
                Authentication user = (Authentication) accessor.getUser();

                if (user == null || !user.isAuthenticated()) {
                    throw new IllegalArgumentException("User is not authenticated for destination: " + destination);
                }

                if (destination != null && destination.contains("/chat/")) {
                    String roomId = extractRoomIdFromDestination(destination);
                    if (roomId != null) {
                        validateRoomAccess(roomId, user.getName());
                    }
                }
            }
        }

        return message;
    }

    private String extractRoomIdFromDestination(String destination) {
        String[] parts = destination.split("/");
        return parts[parts.length - 1];
    }

    private void validateRoomAccess(String roomId, String username) {
        Optional<ChatRoom> roomOpt = chatRoomRepository.findById(roomId);

        if (roomOpt.isEmpty()) {
            throw new IllegalArgumentException("Chat Room does not exist");
        }

        ChatRoom room = roomOpt.get();
        if (!ChatRoomStatus.ACTIVE.equals(room.getStatus())) {
            throw new IllegalArgumentException("Cannot send or receive messages in non-ACTIVE chat room");
        }

        log.debug("Room access validated for room: {} and user: {}", roomId, username);
    }
}