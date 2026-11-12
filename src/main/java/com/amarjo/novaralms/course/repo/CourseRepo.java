package com.amarjo.novaralms.course.repo;

import com.amarjo.novaralms.course.model.CourseStatus;
import com.amarjo.novaralms.course.model.Courses;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepo extends MongoRepository<Courses,String> {
   Optional <Courses> findByCourseCode(String courseCode);
   Optional <List<Courses>> findCoursesByInstructorCode(String instructorCode);
   Optional <List<Courses>> findCoursesByDepartment(String department);
   Optional <List<Courses>> findCoursesByStatus(CourseStatus status);
}
