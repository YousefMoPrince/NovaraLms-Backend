package com.amarjo.novaralms.payment.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PayOnAckRequest {
    private String event;
    private AckData data;
    @Data
    public static class AckData {
        private UUID transactionId;
        private String merchantAccountNumber;
        private String payerAccountNumber;
        private BigDecimal amount;
        private String currency;
        private UUID idempotencyKey;
    }
}