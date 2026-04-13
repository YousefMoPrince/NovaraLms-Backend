package com.amarjo.novaralms.course.repo;

import com.amarjo.novaralms.course.model.courseVideo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface videoRepo extends MongoRepository<courseVideo, String> {
    List<courseVideo> findByCourseCode(String courseCode);
    Optional <courseVideo> findByVideoId(String videoId);
}
