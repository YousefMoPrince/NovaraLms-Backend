package com.amarjo.novaralms.auth.controller;

import com.amarjo.novaralms.auth.DTO.*;
import com.amarjo.novaralms.auth.Services.AuthService;
import com.amarjo.novaralms.auth.model.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RequestMapping("/api/mob/v1/auth")
@RestController
public class MobileAuthController {
    @Autowired
    private AuthService authService;
    public static final Character c = 'M';
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody RegisterRequest registerRequest) {
        UserResponse userResponse = authService.register(registerRequest,c);
        return ResponseEntity.ok(new ApiResponse<>("Register Successful", userResponse));
    }
    @PostMapping("/uploadThumbnail")
    public ResponseEntity<Map<String, Object>> uploadThumbnail(@RequestPart("file") MultipartFile file) {
        String filePath = authService.UploadThumbnail(file,c);
        return ResponseEntity.ok(Map.of(
                "message", "Thumbnail uploaded successfully",
                "data", Map.of("thumbnail_path", filePath)
        ));
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@RequestBody LoginRequest loginRequest) {
        UserResponse userResponse = authService.login(loginRequest,c);
        return ResponseEntity.ok(new ApiResponse<>("Login Successful", userResponse));
    }
    @PatchMapping("/status/change")
    public ResponseEntity<?> changeUserStatus(@RequestParam UserStatus userStatus) {
        authService.changeUserStatus(userStatus,c);
        return ResponseEntity.ok(new ApiResponse<>("Change User Status Successful", userStatus));
    }
    @PostMapping("/refresh")
    public ResponseEntity<Map<String,String>> refreshToken(@RequestBody String refreshToken) {
        return new ResponseEntity<>(authService.refreshToken(refreshToken), HttpStatus.OK);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> requestBody) {
        String refreshToken = requestBody.get("refreshToken");
        authService.mobileLogout(refreshToken);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PutMapping("/update")
    public ResponseEntity<String> update(@RequestBody UpdateRequest updateRequest) {
        authService.updateUser(c,updateRequest);
        return ResponseEntity.ok("Update Successful");
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getUser() {
        UserResponse userResponse = authService.getUser(c);
        return ResponseEntity.ok(new ApiResponse<>("User profile updated successfully",userResponse));
    }
}
