package com.amarjo.novaralms.auth.Services;

import com.amarjo.novaralms.auth.DTO.LoginRequest;
import com.amarjo.novaralms.auth.DTO.RegisterRequest;
import com.amarjo.novaralms.auth.DTO.UserResponse;
import com.amarjo.novaralms.auth.model.users;
import com.amarjo.novaralms.auth.repository.userRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private userRepo userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    public UserResponse register(RegisterRequest registerRequest) {
        users user = new users();
        user.setName(registerRequest.getName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRole(registerRequest.getRole());
        String code = codeUtility.generateUserCode(registerRequest.getRole());
        user.setCode(code);
        userRepo.save(user);
        String token = jwtService.generateToken(user.getName(), user.getEmail(),user.getRole());
        return new UserResponse(user.getUserId(), user.getName(), user.getEmail(), user.getCode(), user.getRole(), token);
    }
    public UserResponse login(LoginRequest loginRequest) {
        users user = userRepo.findByEmail(loginRequest.getEmail()).orElseThrow(()->new RuntimeException("user Not Found"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }
    String token = jwtService.generateToken(user.getName(), user.getEmail(),user.getRole());
    return new UserResponse(user.getUserId(), user.getName(), user.getCode(), user.getEmail(), user.getRole(), token);
    }
}
