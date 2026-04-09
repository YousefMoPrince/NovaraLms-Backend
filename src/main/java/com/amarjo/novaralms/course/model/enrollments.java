package com.amarjo.novaralms.course.model;

import com.amarjo.novaralms.auth.model.users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "enrollments")
public class enrollments {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "enrollment_id" , nullable = false)
    private Long enrollmentId;
    @Column(name = "enrollment_date")
    private Date enrollmentDate;
    private String courseCode;
    @Column(name = "instructor_code", nullable = false)
    private String instructorCode;
    @ManyToOne
    @JoinColumn(name = "student_id" , nullable = false)
    private users studentId;
}
