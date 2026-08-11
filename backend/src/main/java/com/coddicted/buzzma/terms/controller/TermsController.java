package com.coddicted.buzzma.terms.controller;

import com.coddicted.buzzma.shared.security.CurrentUserId;
import com.coddicted.buzzma.terms.dto.TermsAcceptanceStatusDto;
import com.coddicted.buzzma.terms.dto.TermsDto;
import com.coddicted.buzzma.terms.service.TermsService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/terms")
public class TermsController {

  private final TermsService termsService;

  public TermsController(final TermsService termsService) {
    this.termsService = termsService;
  }

  @GetMapping
  public TermsDto get() {
    return this.termsService.getCurrent();
  }

  @GetMapping("/acceptance-status")
  public TermsAcceptanceStatusDto getAcceptanceStatus(@CurrentUserId final UUID requesterId) {
    return this.termsService.getAcceptanceStatus(requesterId);
  }

  @PostMapping("/accept")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void accept(@CurrentUserId final UUID requesterId) {
    this.termsService.recordAcceptance(requesterId);
  }
}
