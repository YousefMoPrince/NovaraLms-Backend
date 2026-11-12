package com.amarjo.novaralms.notification.repository;

import com.amarjo.novaralms.notification.model.UserDeviceToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceTokenRepository extends MongoRepository<UserDeviceToken, String> {
    List<UserDeviceToken> findByUserId(Long userId);
    Optional<UserDeviceToken> findByUserIdAndDeviceToken(Long userId, String deviceToken);
    void deleteByDeviceToken(String deviceToken);
    void deleteByUserIdAndDeviceToken(Long userId, String deviceToken);
}