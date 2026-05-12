package com.coddicted.buzzma.extraction.controller;

import com.coddicted.buzzma.extraction.dto.ExtractionJobResponseDto;
import com.coddicted.buzzma.extraction.mapper.ExtractionMapper;
import com.coddicted.buzzma.extraction.service.ExtractionService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/extraction")
@Validated
public class ExtractionController {

  private final ExtractionService extractionService;
  private final ExtractionMapper extractionMapper;

  public ExtractionController(
      final ExtractionService extractionService, final ExtractionMapper extractionMapper) {
    this.extractionService = extractionService;
    this.extractionMapper = extractionMapper;
  }

  @PostMapping("/sync")
  public ExtractionJobResponseDto extractSync(
      @CurrentUserId final UUID requesterId, @RequestParam("image") final MultipartFile image)
      throws IOException {
    return extractionMapper.toResponse(
        extractionService.extractSync(
            image.getBytes(), image.getOriginalFilename(), image.getContentType(), requesterId));
  }

  @PostMapping("/jobs")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ExtractionJobResponseDto submitJob(
      @CurrentUserId final UUID requesterId, @RequestParam("image") final MultipartFile image)
      throws IOException {
    return extractionMapper.toResponse(
        extractionService.submitJob(
            image.getBytes(), image.getOriginalFilename(), image.getContentType(), requesterId));
  }

  @GetMapping("/jobs/{id}")
  public ExtractionJobResponseDto getJob(
      @CurrentUserId final UUID requesterId, @PathVariable final UUID id) {
    return extractionMapper.toResponse(extractionService.getById(id, requesterId));
  }

  @GetMapping("/jobs")
  public List<ExtractionJobResponseDto> listJobs(@CurrentUserId final UUID requesterId) {
    return extractionService.listByUser(requesterId).stream()
        .map(extractionMapper::toResponse)
        .toList();
  }
}
