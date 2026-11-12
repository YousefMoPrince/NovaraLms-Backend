package com.amarjo.novaralms.course.model;

import com.amarjo.novaralms.auth.model.users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "enrollments")
public class Enrollments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id" , nullable = false)
    private Long enrollmentId;
    @Column(name = "enrollment_date")
    private LocalDateTime enrollmentDate;
    @Column(name = "course_code", nullable = false)
    private String courseCode;
    @Column(name = "instructor_code", nullable = false)
    private String instructorCode;
    @Enumerated(EnumType.STRING)
    @Column(name = "student_status", nullable = false)
    private StudentStatus studentStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_status",nullable = false)
    private EnrollmentStatus enrollmentStatus;
    @Column(name = "content_finished", nullable = false)
    private int contentFinished;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id" , nullable = false)
    private users student;
}
