package com.amarjo.novaralms.course.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Document(collection = "course videos")
public class courseVideo {
    @Id
    private String videoId;
    private String courseCode;
    private String Title;
    private String Description;
    private String videoPath;
    private String thumbnailPath;
    private Integer duration;

}
