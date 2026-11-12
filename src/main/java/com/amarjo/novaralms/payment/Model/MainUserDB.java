package com.amarjo.novaralms.payment.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "main_user")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MainUserDB {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "main_user_id",nullable = false,unique = true,updatable = false)
    private UUID mainUserId;
    @Column(name = "main_user_name",nullable = false,unique = true)
    private String mainUserName;
    @Column(name = "main_user_email",nullable = false,unique = true)
    private String mainUserEmail;
    @Column(name = "main_user_dial",nullable = false,unique = true)
    private String mainUserDial;
    @Column(name = "main_user_role",nullable = false)
    private String mainUserRole;
    @Column(name = "main_user_type",nullable = false)
    private String mainUserType;
    @Column(name = "main_user_status",nullable = false)
    private String mainUserStatus;
    @Column(name = "main_user_account_number",nullable = false,unique = true)
    private Long mainUserAccountNumber;
    @Column(name = "created_at",nullable = false,unique = true,updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at",unique = true)
    private LocalDateTime updatedAt;
}
