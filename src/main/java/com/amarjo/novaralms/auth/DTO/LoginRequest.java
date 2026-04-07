package com.amarjo.novaralms.auth.DTO;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
