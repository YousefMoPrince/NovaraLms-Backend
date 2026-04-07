package com.amarjo.novaralms.auth.controller;

import com.amarjo.novaralms.auth.DTO.ApiResponse;
import com.amarjo.novaralms.auth.DTO.LoginRequest;
import com.amarjo.novaralms.auth.DTO.RegisterRequest;
import com.amarjo.novaralms.auth.DTO.UserResponse;
import com.amarjo.novaralms.auth.Services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody RegisterRequest registerRequest) {
        UserResponse userResponse = authService.register(registerRequest);
        return ResponseEntity.ok(new ApiResponse<>("Register Successful", userResponse));
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@RequestBody LoginRequest loginRequest) {
        UserResponse userResponse = authService.login(loginRequest);
        return ResponseEntity.ok(new ApiResponse<>("Login Successful", userResponse));
    }
}
