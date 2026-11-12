package com.amarjo.novaralms.payment.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptEventResponse {
    private String event;
    private ReceiptResponseData data;
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReceiptResponseData {
        private UUID transactionId;
        private Long accountNumber;
        private Long payerAccountNumber;
        private Long transactionCode;
        private UUID idempotencyKey;
    }
}
