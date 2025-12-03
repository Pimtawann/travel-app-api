package com.techup.travel_app.controller;

import com.techup.travel_app.security.JwtService;
import com.techup.travel_app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
  
    // Register
    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> req) {
      return authService.register(
          req.get("email"),
          req.get("password"),
          req.get("displayName")
      );
    }
  
    // Login
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> req) {
      String token = authService.login(req.get("email"), req.get("password"));
      return Map.of("token", token);
    }
  
    // /auth/me — แสดง email จาก token
    @GetMapping("/me")
    public Map<String, String> me(@RequestHeader("Authorization") String header) {
      String token = header.replace("Bearer ", "");
      String email = jwtService.extractEmail(token);
      return Map.of("email", email);
    }

    // GET /auth/profile — ดึงข้อมูล user profile
    @GetMapping("/profile")
    public Map<String, Object> getProfile(@RequestHeader("Authorization") String header) {
      String token = header.replace("Bearer ", "");
      String email = jwtService.extractEmail(token);
      var user = authService.getUserProfile(email);
      return Map.of(
          "email", user.getEmail(),
          "displayName", user.getDisplayName() != null ? user.getDisplayName() : "",
          "createdAt", user.getCreatedAt().toString()
      );
    }

    // PUT /auth/profile — อัพเดท user profile
    @PutMapping("/profile")
    public Map<String, Object> updateProfile(
        @RequestHeader("Authorization") String header,
        @RequestBody Map<String, String> req
    ) {
      String token = header.replace("Bearer ", "");
      String email = jwtService.extractEmail(token);

      String displayName = req.get("displayName");
      String newPassword = req.get("password");

      var user = authService.updateUserProfile(email, displayName, newPassword);

      return Map.of(
          "message", "Profile updated successfully",
          "email", user.getEmail(),
          "displayName", user.getDisplayName() != null ? user.getDisplayName() : ""
      );
    }

    // POST /auth/forgot-password — ส่ง email สำหรับรีเซ็ตรหัสผ่าน
    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody Map<String, String> req) {
      try {
        authService.requestPasswordReset(req.get("email"));
        return Map.of("message", "If the email exists, a reset link has been sent");
      } catch (Exception e) {
        // Don't reveal whether email exists or not for security
        return Map.of("message", "If the email exists, a reset link has been sent");
      }
    }

    // POST /auth/reset-password — รีเซ็ตรหัสผ่านด้วย token
    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@RequestBody Map<String, String> req) {
      authService.resetPassword(req.get("token"), req.get("newPassword"));
      return Map.of("message", "Password reset successfully");
    }

    // GET /auth/validate-reset-token — ตรวจสอบว่า token ยังใช้งานได้หรือไม่
    @GetMapping("/validate-reset-token")
    public Map<String, Boolean> validateResetToken(@RequestParam String token) {
      boolean isValid = authService.validateResetToken(token);
      return Map.of("valid", isValid);
    }
  }
