package com.amarjo.novaralms.payment.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentApiResponse<T> {
    private String message;
    private T data;
}
