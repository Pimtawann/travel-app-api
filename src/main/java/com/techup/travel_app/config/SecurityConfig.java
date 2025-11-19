package com.techup.travel_app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techup.travel_app.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
  
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
          .cors(cors -> cors.configurationSource(request -> {
              var corsConfig = new org.springframework.web.cors.CorsConfiguration();
              corsConfig.addAllowedOrigin("http://localhost:5173");
              corsConfig.addAllowedOrigin("http://localhost:5174");
              corsConfig.addAllowedOriginPattern("https://*.vercel.app");
              corsConfig.addAllowedOriginPattern("https://*.netlify.app");
              corsConfig.addAllowedMethod("*");
              corsConfig.addAllowedHeader("*");
              corsConfig.setAllowCredentials(true);
              return corsConfig;
          }))
          .csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/auth/**").permitAll()
              .requestMatchers("/api/trips/**").permitAll()
              .anyRequest().authenticated()
          )
          .httpBasic(httpBasic -> httpBasic.disable())
          .sessionManagement(session -> session.sessionCreationPolicy(
              org.springframework.security.config.http.SessionCreationPolicy.STATELESS
          ))
          .exceptionHandling(exception -> exception
              .authenticationEntryPoint((request, response, authException) -> {
                  response.setStatus(HttpStatus.UNAUTHORIZED.value());
                  response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                  Map<String, Object> errorResponse = new HashMap<>();
                  errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                  errorResponse.put("message", "Authentication required");
                  errorResponse.put("timestamp", LocalDateTime.now().toString());
                  errorResponse.put("path", request.getRequestURI());

                  ObjectMapper mapper = new ObjectMapper();
                  response.getWriter().write(mapper.writeValueAsString(errorResponse));
              })
              .accessDeniedHandler((request, response, accessDeniedException) -> {
                  response.setStatus(HttpStatus.FORBIDDEN.value());
                  response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                  Map<String, Object> errorResponse = new HashMap<>();
                  errorResponse.put("status", HttpStatus.FORBIDDEN.value());
                  errorResponse.put("message", "Access denied");
                  errorResponse.put("timestamp", LocalDateTime.now().toString());
                  errorResponse.put("path", request.getRequestURI());

                  ObjectMapper mapper = new ObjectMapper();
                  response.getWriter().write(mapper.writeValueAsString(errorResponse));
              })
          )
          .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
      return http.build();
    }
  
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
    }
  }
