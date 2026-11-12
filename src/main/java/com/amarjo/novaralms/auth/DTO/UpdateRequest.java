package com.amarjo.novaralms.auth.DTO;

import lombok.Data;

@Data
public class UpdateRequest {
    private String name ;
    private String email;
    private String password;
}
