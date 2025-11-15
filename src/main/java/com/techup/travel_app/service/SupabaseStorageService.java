package com.techup.travel_app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

  @Value("${supabase.url}")
  private String supabaseUrl;

  @Value("${supabase.bucket}")
  private String bucket;

  @Value("${supabase.apiKey}")
  private String apiKey;

  private final WebClient.Builder webClientBuilder;

  // Allowed image types
  private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
      "image/jpeg",
      "image/jpg",
      "image/png",
      "image/webp",
      "image/gif"
  );

  // Max file size: 5MB
  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

  // อัปโหลดไฟล์ขึ้น Supabase แล้วคืน public URL
  public String uploadFile(MultipartFile file) {
    // Validate file
    validateFile(file);

    String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file.bin";
    String fileName = System.currentTimeMillis() + "_" + original;
    // Upload to "photo" folder
    String filePath = "photo/" + fileName;
    String uploadUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucket, filePath);

    byte[] bytes;
    try {
      bytes = file.getBytes();
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot read file bytes", e);
    }

    try {
      WebClient webClient = webClientBuilder.build();

      webClient.post()
          .uri(uploadUrl)
          .header("Authorization", "Bearer " + apiKey)
          .contentType(MediaType.parseMediaType(file.getContentType() != null ? file.getContentType() : "application/octet-stream"))
          .bodyValue(bytes)
          .retrieve()
          .onStatus(HttpStatusCode::isError, res ->
              res.bodyToMono(String.class).defaultIfEmpty("Upload failed").flatMap(msg ->
                  Mono.error(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Supabase upload failed: " + msg))
              )
          )
          .toBodilessEntity()
          .block();

      // public URL สำหรับ access ไฟล์ได้ทันที
      return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucket, filePath);

    } catch (ResponseStatusException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unexpected error while uploading to Supabase", ex);
    }
  }

  private void validateFile(MultipartFile file) {
    // Check if file is empty
    if (file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
    }

    // Check file size
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("File size exceeds maximum allowed size of %d MB", MAX_FILE_SIZE / 1024 / 1024));
    }

    // Check content type
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Invalid file type. Only JPEG, PNG, WebP, and GIF images are allowed");
    }
  }
}
