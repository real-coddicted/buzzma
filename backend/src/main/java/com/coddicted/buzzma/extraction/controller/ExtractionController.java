package com.coddicted.buzzma.extraction.controller;

import com.coddicted.buzzma.claim.service.ClaimScreenshotService;
import com.coddicted.buzzma.extraction.entity.ExtractionResult;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/extraction")
@Validated
public class ExtractionController {

  private final ClaimScreenshotService claimScreenshotService;

  public ExtractionController(final ClaimScreenshotService claimScreenshotService) {
    this.claimScreenshotService = claimScreenshotService;
  }

  @PostMapping(value = "/sync", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ExtractionResult extractSync(
      @RequestParam(name = "requesterId", required = false) final UUID requesterId,
      @RequestPart("image") final MultipartFile image)
      throws IOException {
    return claimScreenshotService.extractSync(
        image.getBytes(), image.getOriginalFilename(), image.getContentType(), requesterId);
  }
}
