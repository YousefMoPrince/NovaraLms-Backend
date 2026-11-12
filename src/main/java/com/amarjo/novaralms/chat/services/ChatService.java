package com.amarjo.novaralms.chat.services;

import com.amarjo.novaralms.auth.Services.SessionService;
import com.amarjo.novaralms.auth.model.users;
import com.amarjo.novaralms.auth.repository.UserRepo;
import com.amarjo.novaralms.chat.dto.*;
import com.amarjo.novaralms.chat.model.*;
import com.amarjo.novaralms.chat.repository.ChatMessageRepository;
import com.amarjo.novaralms.chat.repository.ChatRoomRepository;
import com.amarjo.novaralms.course.model.Courses;
import com.amarjo.novaralms.course.model.EnrollmentStatus;
import com.amarjo.novaralms.course.repo.CourseRepo;
import com.amarjo.novaralms.course.repo.EnrollmentRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final EnrollmentRepo enrollmentRepo;
    private final UserRepo userRepo;
    private final CourseRepo courseRepo;
    private final SessionService sessionService;
    private final SimpMessagingTemplate messagingTemplate;

    private String userAuth(Character c){
        String email;
        if (c == 'W') {
            if (sessionService.isSessionActive()) {
                email = sessionService.getEmail();
            } else {
                throw new RuntimeException("Unauthorized: No active Web Session found");
            }
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                email = auth.getName();
            } else {
                throw new RuntimeException("Unauthorized: Invalid or missing Mobile JWT");
            }
        }
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Unauthorized: Invalid or missing Auth");
        }
        return email;
    }
    public ChatRoomResponse requestChatRoom(Character c, CreateChatRoomRequest request) {
        String email = userAuth(c);
        String courseCode = request.getCourseCode();
        Courses course = courseRepo.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Course not found"));
        users instructor = userRepo.findByCode(course.getInstructorCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Instructor not found"));
        users student = userRepo.findByEmail(email)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        boolean hasActiveEnrollment = enrollmentRepo.existsByStudentAndCourseCodeAndEnrollmentStatus(student,courseCode, EnrollmentStatus.ACTIVE);
        if (!hasActiveEnrollment) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You must have to enroll in this course to request a chat.");
        }
        boolean isTheChatActive = chatRoomRepository.existsByStudentIdAndCourseCodeAndInstructorIdAndStatus(student.getUserId(), course.getCourseCode(), instructor.getUserId(), ChatRoomStatus.ACTIVE);
        if (isTheChatActive) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"You are already in chat room");
        }

        ChatRoom room = ChatRoom.builder()
                .courseCode(courseCode)
                .studentId(student.getUserId())
                .instructorId(instructor.getUserId())
                .status(ChatRoomStatus.PENDING)
                .requestedBy(student.getName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(room);
        log.info("Chat room requested [RoomId: {}, Student: {}, Course: {}]", savedRoom.getId(), student.getUserId(), courseCode);

        return mapToChatRoomResponse(savedRoom, student.getUserId());
    }


    public ChatRoomResponse respondToChatRequest(Character c,RespondChatRoomRequest response) {
        String email = userAuth(c);
        users instructor = userRepo.findByEmail(email)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        ChatRoom room = chatRoomRepository.findById(response.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat Room not found"));

        if (!room.getInstructorId().equals(instructor.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to respond to this request.");
        }

        room.setStatus(response.getStatus());
        room.setUpdatedAt(LocalDateTime.now());
        ChatRoom updatedRoom = chatRoomRepository.save(room);

        log.info("Instructor {} updated chat room {} status to {}", instructor.getUserId(), response.getRoomId(), response.getStatus());

        return mapToChatRoomResponse(updatedRoom, instructor.getUserId());
    }


    public ChatMessageResponse sendMessage(SendMessageDto messageDto, Character c) {
        String email = userAuth(c);
        ChatRoom room = chatRoomRepository.findById(messageDto.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat room not found"));
        users sender = userRepo.findByEmail(email)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        Courses course = courseRepo.findByCourseCode(room.getCourseCode())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Course not found"));
        if (!ChatRoomStatus.ACTIVE.equals(room.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot send messages in non-ACTIVE chat room.");
        }
        boolean instructorOfTheCourse = course.getInstructorCode().equals(sender.getCode());
        boolean hasActiveEnrollment = enrollmentRepo.existsByStudentAndCourseCodeAndEnrollmentStatus(sender,room.getCourseCode(), EnrollmentStatus.ACTIVE);
        if (!hasActiveEnrollment && !instructorOfTheCourse) {
            chatRoomRepository.save(room);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course access expired. Chat has been closed.");
        }
        Long receiverId = sender.getUserId().equals(room.getStudentId()) ? room.getInstructorId() : room.getStudentId();

        ChatMessage message = ChatMessage.builder()
                .roomId(messageDto.getRoomId())
                .senderId(sender.getUserId())
                .receiverId(receiverId)
                .content(messageDto.getContent())
                .type(messageDto.getType() != null ? messageDto.getType() : MessageType.TEXT)
                .mediaUrl(messageDto.getMediaUrl())
                .status(MessageStatus.SENT)
                .timestamp(LocalDateTime.now())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        room.setUpdatedAt(LocalDateTime.now());
        room.setLastMessage(ChatRoom.LastMessage.builder()
                .senderId(sender.getUserId())
                .content(savedMessage.getContent())
                .timestamp(savedMessage.getTimestamp())
                .build());

        chatRoomRepository.save(room);

        ChatMessageResponse response = ChatMessageResponse
                .builder()
                .id(savedMessage.getId())
                .roomId(savedMessage.getRoomId())
                .senderId(savedMessage.getSenderId())
                .receiverId(savedMessage.getReceiverId())
                .content(savedMessage.getContent())
                .type(savedMessage.getType() != null ? savedMessage.getType() : MessageType.TEXT)
                .mediaUrl(savedMessage.getMediaUrl())
                .status(savedMessage.getStatus())
                .timestamp(savedMessage.getTimestamp())
                .build();

        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/messages",
                response
        );

        return response;
    }


    public List<ChatRoomResponse> getUserChatRooms(Character c) {
        String email = userAuth(c);
        users user = userRepo.findByEmail(email)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        List<ChatRoomStatus> activeStatuses = List.of(ChatRoomStatus.ACTIVE, ChatRoomStatus.PENDING);
        List<ChatRoom> rooms = chatRoomRepository.findAllByUserIdAndStatusInOrderByUpdatedAtDesc(user.getUserId(), activeStatuses);

        return rooms.stream()
                .map(room -> mapToChatRoomResponse(room, user.getUserId()))
                .collect(Collectors.toList());
    }

    public Slice<ChatMessageResponse> getRoomMessages(Character c,String roomId, int page, int size) {
        String email = userAuth(c);
        users user = userRepo.findByEmail(email)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat Room not found"));

        if (!user.getUserId().equals(room.getStudentId()) && !user.getUserId().equals(room.getInstructorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not belong to this chat room.");
        }

        Pageable pageable = PageRequest.of(page, size);
        Slice<ChatMessage> messagesSlice = chatMessageRepository.findByRoomIdOrderByTimestampDesc(roomId, pageable);
        markMessagesAsRead(roomId, user.getUserId());

        return messagesSlice.map(this::mapToChatMessageResponse);
    }

    private void markMessagesAsRead(String roomId, Long userId) {
        List<ChatMessage> unreadMessages = chatMessageRepository
                .findByRoomIdAndReceiverIdAndStatus(roomId, userId, MessageStatus.SENT);

        if (!unreadMessages.isEmpty()) {
            unreadMessages.forEach(msg -> msg.setStatus(MessageStatus.READ));
            chatMessageRepository.saveAll(unreadMessages);
        }
    }

    private ChatRoomResponse mapToChatRoomResponse(ChatRoom room, Long currentUserId) {
        long unreadCount = chatMessageRepository.countByRoomIdAndReceiverIdAndStatusNot(
                room.getId(), currentUserId, MessageStatus.READ);

        ChatMessageResponse lastMsgDto = null;
        if (room.getLastMessage() != null) {
            lastMsgDto = ChatMessageResponse.builder()
                    .senderId(room.getLastMessage().getSenderId())
                    .content(room.getLastMessage().getContent())
                    .timestamp(room.getLastMessage().getTimestamp())
                    .build();
        }

        return ChatRoomResponse.builder()
                .id(room.getId())
                .courseCode(room.getCourseCode())
                .studentId(room.getStudentId())
                .instructorId(room.getInstructorId())
                .status(room.getStatus())
                .lastMessage(lastMsgDto)
                .unreadCount(unreadCount)
                .updatedAt(room.getUpdatedAt())
                .build();
    }
    private ChatMessageResponse mapToChatMessageResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getRoomId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .type(message.getType())
                .mediaUrl(message.getMediaUrl())
                .status(message.getStatus())
                .timestamp(message.getTimestamp())
                .build();
    }

}