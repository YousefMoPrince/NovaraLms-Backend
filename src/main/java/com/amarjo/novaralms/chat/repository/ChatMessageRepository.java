package com.amarjo.novaralms.chat.repository;

import com.amarjo.novaralms.chat.model.ChatMessage;
import com.amarjo.novaralms.chat.model.MessageStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    Slice<ChatMessage> findByRoomIdOrderByTimestampDesc(String roomId, Pageable pageable);

    long countByRoomIdAndReceiverIdAndStatusNot(String roomId, Long receiverId, MessageStatus status);

    List<ChatMessage> findByRoomIdAndReceiverIdAndStatus(String roomId, Long receiverId, MessageStatus status);
}