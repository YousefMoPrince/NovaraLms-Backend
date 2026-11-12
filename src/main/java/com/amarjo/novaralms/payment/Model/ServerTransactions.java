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
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "server_transactions")
public class ServerTransactions {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "server_transaction_id",nullable = false,updatable = false,unique = true)
    private UUID serverTransactionId;
    @Column(name = "server_transaction_amount",nullable = false)
    private BigDecimal serverTransactionAmount;
    @Column(name = "server_transaction_type",nullable = false)
    private String serverTransactionType;
    @Column(name = "server_transaction_account_number",nullable = false)
    private String serverTransactionAccountNumber;
    @Column(name = "server_transaction_receiver_account_number")
    private String serverTransactionReceiverAccountNumber;
    @Column(name = "server_transaction_status",nullable = false)
    private String serverTransactionStatus;
    @Column(name = "server_transaction_idempotency_key",nullable = false)
    private UUID serverTransactionIdempotencyKey;
    @Column(name = "server_transaction_time_stamp",nullable = false,updatable = false)
    private LocalDateTime serverTransactionTimeStamp;
}
