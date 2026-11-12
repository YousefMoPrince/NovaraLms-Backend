package com.amarjo.novaralms.notification.service;

import com.amarjo.novaralms.auth.Services.SessionService;
import com.amarjo.novaralms.auth.model.users;
import com.amarjo.novaralms.auth.repository.UserRepo;
import com.amarjo.novaralms.notification.dto.*;
import com.amarjo.novaralms.notification.model.Notification;
import com.amarjo.novaralms.notification.model.UserDeviceToken;
import com.amarjo.novaralms.notification.repository.NotificationRepository;
import com.amarjo.novaralms.notification.repository.UserDeviceTokenRepository;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserDeviceTokenRepository tokenRepository;
    private final SessionService sessionService;
    private final UserRepo userRepo;

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
    public void registerDeviceToken(DeviceTokenRequest request, Character c) {
        String email = userAuth(c);
        users user = userRepo.findByEmail(email)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        tokenRepository.findByUserIdAndDeviceToken(user.getUserId(), request.getDeviceToken())
                .ifPresentOrElse(
                        existingToken -> {
                            existingToken.setDeviceType(request.getDeviceType());
                            existingToken.setUpdatedAt(LocalDateTime.now());
                            tokenRepository.save(existingToken);
                        },
                        () -> {
                            UserDeviceToken newToken = UserDeviceToken.builder()
                                    .userId(user.getUserId())
                                    .deviceToken(request.getDeviceToken())
                                    .deviceType(request.getDeviceType())
                                    .updatedAt(LocalDateTime.now())
                                    .build();
                            tokenRepository.save(newToken);
                        }
                );
    }

    public void unregisterDeviceToken(String deviceToken, Character c) {
        String email = userAuth(c);
        users user = userRepo.findByEmail(email)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        tokenRepository.deleteByUserIdAndDeviceToken(user.getUserId(), deviceToken);
    }

    public void sendNotificationToUser(SendNotificationRequest request) {
        Notification notification = Notification.builder()
                .recipientUserId(request.getRecipientUserId())
                .title(request.getTitle())
                .body(request.getBody())
                .imageUrl(request.getImageUrl())
                .type(request.getType())
                .data(request.getData())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        List<UserDeviceToken> userTokens = tokenRepository.findByUserId(request.getRecipientUserId());

        if (userTokens.isEmpty()) {
            log.info("No FCM tokens registered for userId: {}", request.getRecipientUserId());
            return;
        }

        for (UserDeviceToken tokenEntity : userTokens) {
            sendFcmPush(tokenEntity.getDeviceToken(), request);
        }
    }

    public void sendTopicNotification(TopicNotificationRequest request, Character c) {
        String email = userAuth(c);
        users user = userRepo.findByEmail(email)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        try {
            com.google.firebase.messaging.Notification fcmNotification =
                    com.google.firebase.messaging.Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getBody())
                            .build();

            Message.Builder messageBuilder = Message.builder()
                    .setTopic(request.getTopic())
                    .setNotification(fcmNotification);

            if (request.getData() != null && !request.getData().isEmpty()) {
                messageBuilder.putAllData(request.getData());
            }

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("Topic notification sent successfully to topic [{}]: {}", request.getTopic(), response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send notification to topic [{}]: {}", request.getTopic(), e.getMessage());
        }
    }

    public Page<NotificationResponse> getUserNotifications(Character c, Pageable pageable) {
        String email = userAuth(c);
        users user = userRepo.findByEmail(email)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(user.getUserId(), pageable)
                .map(this::mapToResponse);
    }

    public UnreadCountResponse getUnreadCount(Character c) {
        String email = userAuth(c);
        users user = userRepo.findByEmail(email)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        long count = notificationRepository.countByRecipientUserIdAndIsReadFalse(user.getUserId());
        return new UnreadCountResponse(count);
    }

    public void markAsRead(String notificationId, Character c) {
        String email = userAuth(c);
        users user = userRepo.findByEmail(email)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getRecipientUserId().equals(user.getUserId())) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
    }

    public void markAllAsRead(Character c) {
        String email = userAuth(c);
        users user = userRepo.findByEmail(email)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        List<Notification> unreadNotifications = notificationRepository.findByRecipientUserIdAndIsReadFalse(user.getUserId());
        if (!unreadNotifications.isEmpty()) {
            unreadNotifications.forEach(n -> n.setRead(true));
            notificationRepository.saveAll(unreadNotifications);
        }
    }

    private void sendFcmPush(String token, SendNotificationRequest request) {
        try {
            com.google.firebase.messaging.Notification fcmNotification =
                    com.google.firebase.messaging.Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getBody())
                            .setImage(request.getImageUrl())
                            .build();

            Message.Builder messageBuilder = Message.builder()
                    .setToken(token)
                    .setNotification(fcmNotification);

            if (request.getData() != null && !request.getData().isEmpty()) {
                messageBuilder.putAllData(request.getData());
            }

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("FCM push sent successfully to token [{}]: {}", token, response);

        } catch (FirebaseMessagingException e) {
            log.error("Error sending FCM message to token [{}]: {}", token, e.getMessage());
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                log.info("Deleting stale FCM token from Mongo: {}", token);
                tokenRepository.deleteByDeviceToken(token);
            }
        }
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .imageUrl(notification.getImageUrl())
                .type(notification.getType())
                .data(notification.getData())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}