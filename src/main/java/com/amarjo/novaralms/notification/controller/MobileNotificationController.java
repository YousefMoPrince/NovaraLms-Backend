package com.amarjo.novaralms.notification.controller;

import com.amarjo.novaralms.notification.dto.DeviceTokenRequest;
import com.amarjo.novaralms.notification.dto.NotificationResponse;
import com.amarjo.novaralms.notification.dto.TopicNotificationRequest;
import com.amarjo.novaralms.notification.dto.UnreadCountResponse;
import com.amarjo.novaralms.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mob/v1/notification")
@RequiredArgsConstructor
public class MobileNotificationController {

    private final NotificationService notificationService;
    private static final Character c = 'M';

    @PostMapping("/device/token")
    public ResponseEntity<Void> registerToken(@Valid @RequestBody DeviceTokenRequest request) {
        notificationService.registerDeviceToken(request, c);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send/topic")
    public ResponseEntity<Void> sendTopicNotification(@RequestBody TopicNotificationRequest request) {
        notificationService.sendTopicNotification(request,c);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/device/token")
    public ResponseEntity<Void> unregisterToken(@RequestParam String deviceToken) {
        notificationService.unregisterDeviceToken(deviceToken, c);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUserNotifications(c, pageable));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount(c));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id, c);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read/all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead(c);
        return ResponseEntity.ok().build();
    }
}