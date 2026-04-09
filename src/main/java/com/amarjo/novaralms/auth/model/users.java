package com.amarjo.novaralms.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "users")
public class users {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "code",nullable = false)
    private String code;
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    @Column(name = "email", nullable = false, length = 255)
    private String email;
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    @Column(name = "role", nullable = false, length = 30)
    private String role;


}
