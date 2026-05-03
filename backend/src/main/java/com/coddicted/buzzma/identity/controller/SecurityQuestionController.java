package com.coddicted.buzzma.identity.controller;

import com.coddicted.buzzma.identity.dto.SecurityQuestionRequestDto;
import com.coddicted.buzzma.identity.dto.SecurityQuestionResponseDto;
import com.coddicted.buzzma.identity.entity.SecurityQuestion;
import com.coddicted.buzzma.identity.mapper.SecurityAnswerMapper;
import com.coddicted.buzzma.identity.service.SecurityQuestionAnswerService;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security-questions")
@Validated
public class SecurityQuestionController {

  private final SecurityQuestionAnswerService service;
  private final SecurityAnswerMapper answerMapper;

  public SecurityQuestionController(
      final SecurityQuestionAnswerService service, final SecurityAnswerMapper answerMapper) {
    this.service = service;
    this.answerMapper = answerMapper;
  }

  @GetMapping
  public List<SecurityQuestion> list() {
    return service.listSecurityQuestions();
  }

  @PostMapping("/answers")
  @ResponseStatus(HttpStatus.CREATED)
  public SecurityQuestionResponseDto createAnswer(
      @Valid @RequestBody final SecurityQuestionRequestDto request) {
    return answerMapper.toResponse(service.createSecurityAnswer(answerMapper.toEntity(request)));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteQuestion(@PathVariable final UUID id, @CurrentUserId final UUID requesterId) {
    service.deleteSecurityQuestion(id, requesterId);
  }
}
