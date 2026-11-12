package com.amarjo.novaralms.payment.DTO;

import com.amarjo.novaralms.course.model.Currency;
import com.amarjo.novaralms.payment.Model.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ReceiptRequest {
    private PaymentStatus state;
    private ReceiptDataRequest data;
    @Data
    @Builder
    public static class ReceiptDataRequest {
        private BigDecimal amount;
        private String clientName;
        private Long accountNumber;
        private Currency currency;
        private UUID idempotencyKey;
    }
}
