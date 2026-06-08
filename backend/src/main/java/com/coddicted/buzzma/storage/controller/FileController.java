package com.coddicted.buzzma.storage.controller;

import com.coddicted.buzzma.storage.dto.FileUploadResponseDto;
import com.coddicted.buzzma.storage.service.StorageService;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@RestController
@RequestMapping("/api/v1/files")
@Validated
public class FileController {

  private final StorageService storageService;

  public FileController(final StorageService storageService) {
    this.storageService = storageService;
  }

  // Todo: Add currentUserId
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public FileUploadResponseDto upload(
      @RequestParam("folder") final String folder, @RequestParam("file") final MultipartFile file)
      throws IOException {
    final String storageKey =
        this.storageService.store(
            folder, file.getOriginalFilename(), file.getContentType(), file.getBytes());
    return FileUploadResponseDto.builder().storageKey(storageKey).build();
  }

  // Todo: Add currentUserId
  @GetMapping
  public ResponseEntity<byte[]> retrieve(@RequestParam("key") final String storageKey) {
    final ResponseBytes<GetObjectResponse> response = this.storageService.retrieve(storageKey);
    final String contentType = response.response().contentType();
    final MediaType mediaType =
        contentType != null
            ? MediaType.parseMediaType(contentType)
            : MediaType.APPLICATION_OCTET_STREAM;
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
        .contentType(mediaType)
        .body(response.asByteArray());
  }
}
