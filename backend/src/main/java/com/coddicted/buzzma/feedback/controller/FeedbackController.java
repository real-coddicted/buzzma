package com.coddicted.buzzma.feedback.controller;

import com.coddicted.buzzma.feedback.dto.FeedbackRequestDto;
import com.coddicted.buzzma.feedback.dto.FeedbackResponseDto;
import com.coddicted.buzzma.feedback.entity.Feedback;
import com.coddicted.buzzma.feedback.mapper.FeedbackMapper;
import com.coddicted.buzzma.feedback.service.FeedbackService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feedback")
@Validated
public class FeedbackController {

  private final FeedbackService feedbackService;
  private final FeedbackMapper feedbackMapper;

  public FeedbackController(
      final FeedbackService feedbackService, final FeedbackMapper feedbackMapper) {
    this.feedbackService = feedbackService;
    this.feedbackMapper = feedbackMapper;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public FeedbackResponseDto create(
      @CurrentUserId final UUID requesterId, @Valid @RequestBody final FeedbackRequestDto request) {
    final Feedback feedback =
        this.feedbackMapper.toEntity(request).toBuilder().userId(requesterId).build();
    return this.feedbackMapper.toResponse(this.feedbackService.create(feedback, requesterId));
  }

  @GetMapping("/{id}")
  public FeedbackResponseDto getById(@PathVariable final UUID id) {
    return this.feedbackMapper.toResponse(this.feedbackService.getById(id));
  }

  @GetMapping("/user/{userId}")
  public List<FeedbackResponseDto> listByUserId(@PathVariable final UUID userId) {
    return this.feedbackService.listByUserId(userId).stream()
        .map(this.feedbackMapper::toResponse)
        .toList();
  }
}
