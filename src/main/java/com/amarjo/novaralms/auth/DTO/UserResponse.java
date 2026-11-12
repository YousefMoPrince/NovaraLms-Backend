package com.amarjo.novaralms.auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long userId;
    private String name;
    private String code;
    private String email;
    private String role;
    private String thumbnailPath;
    private Long accountNumber;
    private Boolean isReadyForPayment;
    private String accessToken;
    private String refreshToken;
}
