package com.amarjo.novaralms.payment.DTO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Builder
public class TransferRequest {
    private BigDecimal amount;
    private String idempotencyKey;
    private Long receiverAccountNumber;
}
