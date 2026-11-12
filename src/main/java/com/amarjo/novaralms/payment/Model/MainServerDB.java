package com.amarjo.novaralms.payment.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "main_server")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MainServerDB {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "server_id",unique = true, nullable = false,updatable = false)
    private UUID serverId;
    @Column(name = "company_name",nullable = false,unique = true)
    private String companyName;
    @Column(name = "company_email",nullable = false,unique = true)
    private String companyEmail;
    @Column(name = "company_dial",nullable = false,unique = true)
    private String companyDial;
    @Column(name = "company_password",nullable = false)
    private String companyPassword;
    @Column(name = "company_role",nullable = false,unique = true)
    private String companyRole;
    @Column(name = "company_type",nullable = false,unique = true)
    private String companyType;
    @Column(name = "company_status",nullable = false,unique = true)
    private String companyStatus;
    @Column(name = "company_tax_id",nullable = false,unique = true)
    private String companyTaxId;
    @Column(name = "company_account_number",nullable = false,unique = true)
    private Long companyAccountNumber;
    @Column(name = "company_balance",nullable = false,unique = true)
    private BigDecimal companyBalance;
    @Column(name = "company_api_key",nullable = false,unique = true)
    private String companyApiKey;
    @Column(name = "company_api_secret",nullable = false,unique = true)
    private String companyApiSecret;
    @Column(name = "company_url",nullable = false,unique = true)
    private String companyUrl;
    @Column(name = "created_at",nullable = false,unique = true,updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at",unique = true)
    private LocalDateTime updatedAt;
}
