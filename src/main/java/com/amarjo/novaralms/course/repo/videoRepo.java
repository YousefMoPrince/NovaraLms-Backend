package com.amarjo.novaralms.course.repo;

import com.amarjo.novaralms.course.model.courseVideo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface videoRepo extends MongoRepository<courseVideo, String> {
    List<courseVideo> findByCourseCode(String courseCode);
     courseVideo findByVideoId(String videoId);
}
