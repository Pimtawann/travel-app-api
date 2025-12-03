package com.techup.travel_app.service;

import com.techup.travel_app.entity.PasswordResetToken;
import com.techup.travel_app.entity.User;
import com.techup.travel_app.exception.EmailAlreadyExistsException;
import com.techup.travel_app.exception.InvalidCredentialsException;
import com.techup.travel_app.exception.ResourceNotFoundException;
import com.techup.travel_app.repository.PasswordResetTokenRepository;
import com.techup.travel_app.repository.UserRepository;
import com.techup.travel_app.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
  
    // สมัครสมาชิกใหม่
    public String register(String email, String password, String displayName) {
      if (userRepository.findByEmail(email).isPresent()) {
        throw new EmailAlreadyExistsException(email);
      }

      User user = User.builder()
          .email(email)
          .passwordHash(passwordEncoder.encode(password))
          .displayName(displayName)
          .build();

      userRepository.save(user);
      return "Registered successfully";
    }
  
    // เข้าสู่ระบบและสร้าง token
    public String login(String email, String password) {
      User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new InvalidCredentialsException());

      if (!passwordEncoder.matches(password, user.getPasswordHash())) {
        throw new InvalidCredentialsException();
      }

      return jwtService.generateToken(user.getEmail(), user.getDisplayName());
    }

    // ดึงข้อมูล user profile
    public User getUserProfile(String email) {
      return userRepository.findByEmail(email)
          .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // อัพเดท user profile
    public User updateUserProfile(String email, String displayName, String newPassword) {
      User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new RuntimeException("User not found"));

      // Update displayName if provided
      if (displayName != null && !displayName.trim().isEmpty()) {
        user.setDisplayName(displayName);
      }

      // Update password if provided
      if (newPassword != null && !newPassword.trim().isEmpty()) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
      }

      return userRepository.save(user);
    }

    // Request password reset
    @Transactional
    public void requestPasswordReset(String email) {
      User user = userRepository.findByEmail(email)
          .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

      // Delete old tokens for this user
      passwordResetTokenRepository.deleteByUser(user);

      // Generate new token
      String token = UUID.randomUUID().toString();
      LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

      PasswordResetToken resetToken = new PasswordResetToken(token, user, expiryDate);
      passwordResetTokenRepository.save(resetToken);

      // Send email with reset link
      emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    // Reset password with token
    @Transactional
    public void resetPassword(String token, String newPassword) {
      PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
          .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

      if (resetToken.isExpired() || resetToken.isUsed()) {
        throw new RuntimeException("Invalid or expired reset token");
      }

      // Validate password
      if (newPassword == null || newPassword.length() < 6) {
        throw new RuntimeException("Password must be at least 6 characters");
      }

      User user = resetToken.getUser();
      user.setPasswordHash(passwordEncoder.encode(newPassword));
      userRepository.save(user);

      // Mark token as used
      resetToken.setUsed(true);
      passwordResetTokenRepository.save(resetToken);
    }

    // Validate reset token
    public boolean validateResetToken(String token) {
      return passwordResetTokenRepository.findByToken(token)
          .map(t -> !t.isExpired() && !t.isUsed())
          .orElse(false);
    }
  }
