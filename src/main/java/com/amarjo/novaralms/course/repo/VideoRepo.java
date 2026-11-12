package com.amarjo.novaralms.course.repo;

import com.amarjo.novaralms.course.model.CourseVideo;
import com.amarjo.novaralms.course.model.VideoStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface VideoRepo extends MongoRepository<CourseVideo, String> {
    List<CourseVideo> findByCourseCode(String courseCode);
    Optional <CourseVideo> findByVideoId(String videoId);
    List<CourseVideo> findByCourseCodeOrderByOrderIndexAsc(String courseCode);
    List<CourseVideo> findByStatusOrderByOrderIndexAsc(VideoStatus status);
}
