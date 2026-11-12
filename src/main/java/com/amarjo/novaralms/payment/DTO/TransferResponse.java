package com.amarjo.novaralms.payment.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferResponse {
    private UUID transactionId;
    private BigDecimal amount;
    private String type;
    private Long receiverAccountNumber;
    private String status;
    private String idempotencyKey;
    private LocalDateTime timestamp;
}
