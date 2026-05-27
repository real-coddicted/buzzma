package com.coddicted.buzzma.extraction.service.impl;

import com.coddicted.buzzma.extraction.entity.ExtractionJob;
import com.coddicted.buzzma.extraction.entity.ExtractionJobStatus;
import com.coddicted.buzzma.extraction.entity.ExtractionResult;
import com.coddicted.buzzma.extraction.entity.ValidationError;
import com.coddicted.buzzma.extraction.persistence.ExtractionJobRepository;
import com.coddicted.buzzma.extraction.service.ExtractionResultValidator;
import com.coddicted.buzzma.extraction.service.ExtractionService;
import com.coddicted.buzzma.extraction.service.GeminiExtractionPromptBuilder;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import com.coddicted.buzzma.shared.exception.ForbiddenException;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import com.coddicted.buzzma.shared.gemini.GeminiClient;
import com.coddicted.buzzma.shared.gemini.GeminiException;
import com.coddicted.buzzma.storage.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExtractionServiceImpl extends BaseCrudService implements ExtractionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExtractionServiceImpl.class);
  private static final int MAX_ATTEMPTS = 3;
  private static final String STORAGE_FOLDER = "extractions";

  private final ExtractionJobRepository jobRepository;
  private final GeminiClient geminiClient;
  private final GeminiExtractionPromptBuilder promptBuilder;
  private final ExtractionResultValidator validator;
  private final StorageService storageService;
  private final ObjectMapper objectMapper;

  public ExtractionServiceImpl(
      final ExtractionJobRepository jobRepository,
      final GeminiClient geminiClient,
      final GeminiExtractionPromptBuilder promptBuilder,
      final ExtractionResultValidator validator,
      final StorageService storageService,
      final ObjectMapper objectMapper) {
    this.jobRepository = jobRepository;
    this.geminiClient = geminiClient;
    this.promptBuilder = promptBuilder;
    this.validator = validator;
    this.storageService = storageService;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional
  public ExtractionResult extractSync(
      final byte[] imageBytes,
      final String originalFilename,
      final String contentType,
      final UUID requesterId) {
    LOGGER.debug("extractSync: starting for requester {}", requesterId);

    final ExtractionResult result;
    try {
      result = callGemini(imageBytes, contentType);
    } catch (final GeminiException e) {
      LOGGER.warn(
          "extractSync: Gemini call failed for requester {}: {}", requesterId, e.getMessage());
      throw new BusinessRuleViolationException("Extraction failed: " + e.getMessage());
    }

    final List<ValidationError> errors = this.validator.validate(result);
    if (!errors.isEmpty()) {
      final String errorSummary =
          errors.stream()
              .map(ve -> ve.getField() + ": " + ve.getMessage())
              .collect(Collectors.joining("; "));
      LOGGER.warn("extractSync: validation failed for requester {}: {}", requesterId, errorSummary);
      result.getValidationErrors().addAll(errors);
    }

    return result;
  }

  @Override
  @Transactional
  public ExtractionJob submitJob(
      final byte[] imageBytes,
      final String originalFilename,
      final String contentType,
      final UUID requesterId) {
    LOGGER.debug("submitJob: storing image for requester {}", requesterId);
    final String storageKey =
        this.storageService.store(STORAGE_FOLDER, originalFilename, contentType, imageBytes);

    final ExtractionJob job =
        ExtractionJob.builder()
            .submittedBy(requesterId)
            .status(ExtractionJobStatus.EXTRACTION_JOB_STATUS_PENDING)
            .storageKey(storageKey)
            .originalFilename(originalFilename)
            .contentType(contentType)
            .attemptCount(0)
            .isDeleted(false)
            .createdBy(requesterId)
            .updatedBy(requesterId)
            .build();
    final ExtractionJob saved = this.jobRepository.save(job);
    LOGGER.debug("submitJob: created job {} for requester {}", saved.getId(), requesterId);
    return saved;
  }

  @Override
  @Transactional
  public ExtractionJob processJob(final UUID jobId) {
    LOGGER.debug("processJob: starting job {}", jobId);
    ExtractionJob job =
        this.jobRepository
            .findById(jobId)
            .orElseThrow(() -> new NotFoundException("ExtractionJob not found: " + jobId));

    job =
        job.toBuilder()
            .status(ExtractionJobStatus.EXTRACTION_JOB_STATUS_PROCESSING)
            .attemptCount(job.getAttemptCount() + 1)
            .updatedBy(job.getSubmittedBy())
            .build();
    job = this.jobRepository.save(job);

    try {
      final byte[] imageBytes = this.storageService.retrieve(job.getStorageKey());
      final ExtractionResult result = callGemini(imageBytes, job.getContentType());
      final List<ValidationError> errors = this.validator.validate(result);

      job =
          job.toBuilder()
              .status(ExtractionJobStatus.EXTRACTION_JOB_STATUS_COMPLETED)
              .result(result)
              .validationErrors(errors.isEmpty() ? null : errors)
              .errorMessage(null)
              .build();
      LOGGER.debug("processJob: completed job {}", jobId);
    } catch (final GeminiException e) {
      final boolean exhausted = job.getAttemptCount() >= MAX_ATTEMPTS;
      LOGGER.warn(
          "processJob: Gemini failed for job {} (attempt {}/{}): {}",
          jobId,
          job.getAttemptCount(),
          MAX_ATTEMPTS,
          e.getMessage());
      job =
          job.toBuilder()
              .status(
                  exhausted
                      ? ExtractionJobStatus.EXTRACTION_JOB_STATUS_FAILED
                      : ExtractionJobStatus.EXTRACTION_JOB_STATUS_PENDING)
              .errorMessage(e.getMessage())
              .build();
    }
    return this.jobRepository.save(job);
  }

  @Override
  @Transactional(readOnly = true)
  public ExtractionJob getById(final UUID jobId, final UUID requesterId) {
    final ExtractionJob job =
        this.jobRepository
            .findByIdAndIsDeletedFalse(jobId)
            .orElseThrow(() -> new NotFoundException("ExtractionJob not found: " + jobId));
    if (!job.getSubmittedBy().equals(requesterId)) {
      throw new ForbiddenException("Access denied to extraction job: " + jobId);
    }
    return job;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ExtractionJob> listByUser(final UUID requesterId) {
    return this.jobRepository.findBySubmittedByAndIsDeletedFalse(requesterId);
  }

  private ExtractionResult callGemini(final byte[] imageBytes, final String mimeType) {
    final String rawText =
        this.geminiClient.generateContent(this.promptBuilder.build(), imageBytes, mimeType);
    final String json = sanitizeJson(rawText);
    try {
      return this.objectMapper.readValue(json, ExtractionResult.class);
    } catch (final Exception e) {
      throw new GeminiException("Failed to parse Gemini response as ExtractionResult: " + json, e);
    }
  }

  private String sanitizeJson(final String raw) {
    String trimmed = raw.strip();
    if (trimmed.startsWith("```")) {
      trimmed = trimmed.replaceFirst("```[a-z]*\\n?", "");
      final int lastFence = trimmed.lastIndexOf("```");
      if (lastFence >= 0) {
        trimmed = trimmed.substring(0, lastFence);
      }
      trimmed = trimmed.strip();
    }
    return trimmed;
  }
}
