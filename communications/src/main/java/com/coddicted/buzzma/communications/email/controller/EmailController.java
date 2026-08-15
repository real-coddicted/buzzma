package com.coddicted.buzzma.communications.email.controller;

import com.coddicted.buzzma.communications.email.config.EmailProperties;
import com.coddicted.buzzma.communications.email.dto.EmailStatusResponseDto;
import com.coddicted.buzzma.communications.email.dto.SendEmailRequestDto;
import com.coddicted.buzzma.communications.email.dto.SendEmailResponseDto;
import com.coddicted.buzzma.communications.email.mapper.EmailCommunicationLogMapper;
import com.coddicted.buzzma.communications.email.model.EmailCommunicationLog;
import com.coddicted.buzzma.communications.email.publisher.EmailOutboxPublisher;
import com.coddicted.buzzma.communications.email.service.EmailCommunicationLogService;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Excluded from the {@code worker} profile — that instance has no web layer. */
@RestController
@RequestMapping("/api/email/messages")
@Profile("!worker")
public class EmailController {

  private static final String RETRY_AFTER_SECONDS = "5";

  private final EmailProperties emailProperties;
  private final EmailCommunicationLogService logService;
  private final EmailCommunicationLogMapper mapper;
  private final EmailOutboxPublisher outboxPublisher;

  public EmailController(
      final EmailProperties emailProperties,
      final EmailCommunicationLogService logService,
      final EmailCommunicationLogMapper mapper,
      final EmailOutboxPublisher outboxPublisher) {
    this.emailProperties = emailProperties;
    this.logService = logService;
    this.mapper = mapper;
    this.outboxPublisher = outboxPublisher;
  }

  @PostMapping
  public ResponseEntity<SendEmailResponseDto> send(@RequestBody final SendEmailRequestDto request) {
    final EmailCommunicationLog log =
        this.logService.createPending(
            request.getTo(),
            this.emailProperties.getFromAddress(),
            request.getSubject(),
            parseUuid(request.getRequestedBy()));

    try {
      this.outboxPublisher.publish(
          log.getId(), request.getTo(), request.getSubject(), request.getBody());
    } catch (final RuntimeException ex) {
      final EmailCommunicationLog failed = this.logService.markFailed(log, ex.getMessage());
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
          .header(HttpHeaders.RETRY_AFTER, RETRY_AFTER_SECONDS)
          .body(this.mapper.toSendResponse(failed));
    }

    return ResponseEntity.status(HttpStatus.ACCEPTED).body(this.mapper.toSendResponse(log));
  }

  @GetMapping("/{requestId}")
  public ResponseEntity<EmailStatusResponseDto> getStatus(@PathVariable final UUID requestId) {
    return this.logService
        .findById(requestId)
        .map(log -> ResponseEntity.ok(this.mapper.toStatusResponse(log)))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  private static UUID parseUuid(final String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return UUID.fromString(value);
    } catch (final IllegalArgumentException ex) {
      return null;
    }
  }
}
