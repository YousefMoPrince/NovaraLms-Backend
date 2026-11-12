package com.amarjo.novaralms.payment.Model;

import com.amarjo.novaralms.course.model.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "course_payment")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoursePayment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", nullable = false, updatable = false, unique = true)
    private UUID paymentId;
    @Column(name = "course_code",nullable = false)
    private String courseCode;
    @Column(name = "payment_code",unique = true)
    private Long paymentCode;
    @Column(name = "account_number", nullable = false)
    private Long accountNumber;
    @Column(name = "payer_account_number")
    private Long payerAccountNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus paymentStatus;
    @Column(name = "amount",nullable = false)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(name = "currency",updatable = false)
    private Currency currency;
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    @Column(name = "idempotency_key",nullable = false,updatable = false)
    private UUID idempotencyKey;
}
