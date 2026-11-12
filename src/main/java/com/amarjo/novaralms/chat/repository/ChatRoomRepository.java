package com.amarjo.novaralms.chat.repository;

import com.amarjo.novaralms.chat.model.ChatRoom;
import com.amarjo.novaralms.chat.model.ChatRoomStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    Optional<ChatRoom> findByStudentIdAndCourseCodeAndInstructorId(Long studentId, String courseCode, Long instructorId);

    @Query("{ '$or': [ { 'studentId': ?0 }, { 'instructorId': ?0 } ], 'status': { '$in': ?1 } }")
    List<ChatRoom> findAllByUserIdAndStatusInOrderByUpdatedAtDesc(Long userId, List<ChatRoomStatus> statuses);

    boolean existsByStudentIdAndCourseCodeAndInstructorIdAndStatus(
            Long studentId, String courseCode, Long instructorId, ChatRoomStatus status
    );
}