package com.techup.travel_app.service;

import com.techup.travel_app.entity.User;
import com.techup.travel_app.exception.EmailAlreadyExistsException;
import com.techup.travel_app.exception.InvalidCredentialsException;
import com.techup.travel_app.repository.UserRepository;
import com.techup.travel_app.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;
  
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
  }
