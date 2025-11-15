package com.techup.travel_app.controller;

import com.techup.travel_app.security.JwtService;
import com.techup.travel_app.service.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final SupabaseStorageService supabaseStorageService;
    private final JwtService jwtService;

    // POST /api/files/upload - อัปโหลดรูปภาพ (protected - ต้อง login)
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
        @RequestParam("file") MultipartFile file,
        @RequestHeader("Authorization") String header
    ) {
      // Verify JWT token
      String token = header.replace("Bearer ", "");
      String email = jwtService.extractEmail(token);

      // Upload file to Supabase Storage (photo folder)
      String url = supabaseStorageService.uploadFile(file);

      return ResponseEntity.ok(Map.of(
          "url", url,
          "message", "File uploaded successfully"
      ));
    }
  }
