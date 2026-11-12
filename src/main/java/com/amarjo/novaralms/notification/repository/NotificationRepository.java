package com.amarjo.novaralms.notification.repository;

import com.amarjo.novaralms.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId, Pageable pageable);
    long countByRecipientUserIdAndIsReadFalse(Long recipientUserId);
    List<Notification> findByRecipientUserIdAndIsReadFalse(Long recipientUserId);
}