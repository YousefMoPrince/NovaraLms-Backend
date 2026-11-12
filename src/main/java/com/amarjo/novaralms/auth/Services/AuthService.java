package com.amarjo.novaralms.auth.Services;

import com.amarjo.novaralms.auth.DTO.LoginRequest;
import com.amarjo.novaralms.auth.DTO.RegisterRequest;
import com.amarjo.novaralms.auth.DTO.UpdateRequest;
import com.amarjo.novaralms.auth.DTO.UserResponse;
import com.amarjo.novaralms.auth.model.UserStatus;
import com.amarjo.novaralms.auth.model.users;
import com.amarjo.novaralms.auth.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class AuthService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private SessionService sessionService;

    private String userAuth(Character c){
        String email;
        if (c == 'W') {
            if (sessionService.isSessionActive()) {
                email = sessionService.getEmail();
            } else {
                throw new RuntimeException("Unauthorized: No active Web Session found");
            }
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                email = auth.getName();
            } else {
                throw new RuntimeException("Unauthorized: Invalid or missing Mobile JWT");
            }
        }
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Unauthorized: Invalid or missing Auth");
        }
        return email;
    }
    public UserResponse register(RegisterRequest registerRequest, Character c) {
        users user = new users();
        user.setName(registerRequest.getName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRole(registerRequest.getRole());
        user.setIsReadyForPayment(false);
        user.setUserStatus(UserStatus.ONLINE);
        String code = codeUtility.generateUserCode(registerRequest.getRole());
        user.setCode(code);
        userRepo.save(user);
        if (c.equals('M')) {
            Map<String, String> token = jwtService.generateTokens(user.getName(), user.getEmail(), user.getRole());
            return new UserResponse(user.getUserId(), user.getName(), user.getCode(), user.getEmail(), user.getRole(),null,null,false, token.get("accessToken"), token.get("refreshToken"));
        } else {
            sessionService.createSession(user.getName(), user.getEmail(), user.getRole());
            return new UserResponse(user.getUserId(), user.getName(), user.getCode(), user.getEmail(), user.getRole(),null,null,false, null, null);

        }

    }

    public String UploadThumbnail(MultipartFile file, Character c) {
        try {
            String email = userAuth(c);
            users user = userRepo.findByEmail(email).orElseThrow(
                    () -> new RuntimeException("Unauthorized: Invalid or missing Mobile JWT"));
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("User thumbnail file is missing or empty");
            }
            String fileName = user.getCode() + ".jpg";
            String uploadDir = System.getProperty("user.dir") + File.separator + "storage" + File.separator + "Thumbnail" + File.separator + "userThumbnail";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File destinationFile = new File(directory, fileName);
            try {
                file.transferTo(destinationFile);
            } catch (IOException e) {
                throw new IOException("Critical error during user thumbnail upload: " + e.getMessage());
            }
            String relativePath = "storage/Thumbnail/userThumbnail/" + fileName;
            user.setThumbnailPath(relativePath);
            userRepo.save(user);

            return user.getThumbnailPath();
        } catch (Exception e) {
            throw new RuntimeException("Upload thumbnail failed");
        }
    }

    public UserResponse login(LoginRequest loginRequest, Character c) {
        users user = userRepo.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new RuntimeException("user Not Found"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }
        if (c.equals('M')) {
            Map<String, String> token = jwtService.generateTokens(user.getName(), user.getEmail(), user.getRole());
            user.setUserStatus(UserStatus.ONLINE);
            userRepo.save(user);
            return new UserResponse(user.getUserId(), user.getName(), user.getCode(), user.getEmail(), user.getRole(),user.getThumbnailPath(),user.getAccountNumber(),user.getIsReadyForPayment(), token.get("accessToken"), token.get("refreshToken"));
        } else {
            user.setUserStatus(UserStatus.ONLINE);
            userRepo.save(user);
            sessionService.createSession(user.getName(), user.getEmail(), user.getRole());
            return new UserResponse(user.getUserId(), user.getName(), user.getCode(), user.getEmail(), user.getRole(),user.getThumbnailPath(),user.getAccountNumber(),user.getIsReadyForPayment(), null, null);

        }
    }

    public void updateUser(Character c, UpdateRequest updateRequest) {
    try {
        String email = userAuth(c);
        users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (updateRequest.getName() != null) user.setName(updateRequest.getName());
        if (updateRequest.getEmail() != null) user.setEmail(updateRequest.getEmail());
        if (updateRequest.getPassword() != null) user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        userRepo.save(user);
        } catch (Exception e) {
        throw new RuntimeException(e.getMessage());
        }

    }

    public Map<String, String> refreshToken(String refreshToken) {
        String email = refreshTokenService.getEmailFromToken(refreshToken);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired");
        }
        refreshTokenService.deleteRefreshToken(refreshToken);
        users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return jwtService.generateTokens(user.getName(), user.getEmail(), user.getRole());
    }
    public void changeUserStatus(UserStatus userStatus, Character c) {
        String email = userAuth(c);
      users user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUserStatus(userStatus);
        userRepo.save(user);
    }
    public UserResponse getUser(Character c){
        try {
            String email = null;
            if (c == 'W') {
                if (sessionService.isSessionActive()) {
                    email = sessionService.getEmail();
                } else {
                    throw new RuntimeException("Unauthorized: No active Web Session found");
                }
                users user = userRepo.findByEmail(email).orElseThrow(
                        () -> new RuntimeException("User not found"));
                return new UserResponse(user.getUserId(), user.getName(), user.getCode(), user.getEmail(), user.getRole(),user.getThumbnailPath(),user.getAccountNumber(),user.getIsReadyForPayment(), null, null);
            } else {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                    email = auth.getName();
                    users user = userRepo.findByEmail(email).orElseThrow(
                            () -> new RuntimeException("User not found"));
                    return new UserResponse(user.getUserId(), user.getName(), user.getCode(), user.getEmail(), user.getRole(),user.getThumbnailPath(),user.getAccountNumber(),user.getIsReadyForPayment(), null, null);
                } else {
                    throw new RuntimeException("Unauthorized: Invalid or missing Mobile JWT");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void webLogout() {
        try {
            if (sessionService.isSessionActive()) {
                sessionService.invalidateSession();
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void mobileLogout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenService.deleteRefreshToken(refreshToken);
        }
        SecurityContextHolder.clearContext();
    }

}