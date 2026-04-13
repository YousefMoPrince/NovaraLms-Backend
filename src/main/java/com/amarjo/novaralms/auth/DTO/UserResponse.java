package com.amarjo.novaralms.auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String name;
    private String code;
    private String email;
    private String role;
    private String Token;
}
