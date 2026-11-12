package com.amarjo.novaralms.course.model;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Document(collection = "course_videos")
public class CourseVideo {
    @Id
    private String videoId;
    private String courseCode;
    private String title;
    private String description;
    private String videoPath;
    private String thumbnailPath;
    private Integer durationInSeconds;
    private Integer orderIndex;
    private VideoStatus status;
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
