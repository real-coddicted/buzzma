package com.coddicted.buzzma.communications.email.service;

import com.coddicted.buzzma.communications.email.model.EmailCommunicationLog;
import com.coddicted.buzzma.communications.email.model.EmailStatus;
import com.coddicted.buzzma.communications.email.repository.EmailCommunicationLogRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EmailCommunicationLogService {

  private final EmailCommunicationLogRepository repository;

  public EmailCommunicationLogService(final EmailCommunicationLogRepository repository) {
    this.repository = repository;
  }

  public EmailCommunicationLog createPending(
      final String to, final String from, final String subject, final UUID requestedBy) {
    final EmailCommunicationLog log =
        EmailCommunicationLog.builder()
            .toAddress(to)
            .fromAddress(from)
            .subject(subject)
            .status(EmailStatus.EMAIL_STATUS_PENDING)
            .createdBy(requestedBy)
            .updatedBy(requestedBy)
            .build();
    return this.repository.save(log);
  }

  public EmailCommunicationLog markSent(final EmailCommunicationLog log) {
    log.setStatus(EmailStatus.EMAIL_STATUS_SENT);
    log.setSentAt(Instant.now());
    return this.repository.save(log);
  }

  public EmailCommunicationLog markFailed(
      final EmailCommunicationLog log, final String errorMessage) {
    log.setStatus(EmailStatus.EMAIL_STATUS_FAILED);
    log.setErrorMessage(errorMessage);
    return this.repository.save(log);
  }

  public Optional<EmailCommunicationLog> findById(final UUID requestId) {
    return this.repository.findById(requestId);
  }
}
