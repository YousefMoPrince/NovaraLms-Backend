package com.amarjo.novaralms.course.repo;

import com.amarjo.novaralms.course.model.Courses;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface courseRepo extends MongoRepository<Courses,String> {
   Optional <Courses> findByCourseCode(String courseCode);
   Optional <List<Courses>> findCoursesByInstructorCode(String instructorCode);
}
