package com.coddicted.buzzma.communications.email.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coddicted.buzzma.communications.email.model.EmailCommunicationLog;
import com.coddicted.buzzma.communications.email.model.EmailStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class EmailCommunicationLogRepositoryTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private EmailCommunicationLogRepository repository;

  @Test
  void savesAndReloadsAPendingLog() {
    final UUID requestedBy = UUID.randomUUID();
    final EmailCommunicationLog log =
        EmailCommunicationLog.builder()
            .toAddress("someone@example.com")
            .fromAddress("donotreply@buzzmah.com")
            .subject("Your OTP code")
            .status(EmailStatus.EMAIL_STATUS_PENDING)
            .createdBy(requestedBy)
            .updatedBy(requestedBy)
            .build();

    final EmailCommunicationLog saved = this.repository.save(log);

    assertNotNull(saved.getId());
    assertNotNull(saved.getCreatedAt());
    assertNotNull(saved.getUpdatedAt());

    final Optional<EmailCommunicationLog> reloaded = this.repository.findById(saved.getId());
    assertTrue(reloaded.isPresent());
    assertEquals(EmailStatus.EMAIL_STATUS_PENDING, reloaded.get().getStatus());
    assertEquals(requestedBy, reloaded.get().getCreatedBy());
  }

  @Test
  void updatesStatusAndSentAtOnExistingLog() {
    final EmailCommunicationLog saved =
        this.repository.save(
            EmailCommunicationLog.builder()
                .toAddress("someone@example.com")
                .fromAddress("donotreply@buzzmah.com")
                .subject("Your OTP code")
                .status(EmailStatus.EMAIL_STATUS_PENDING)
                .build());

    saved.setStatus(EmailStatus.EMAIL_STATUS_SENT);
    saved.setSentAt(Instant.now());
    this.repository.save(saved);

    final EmailCommunicationLog reloaded = this.repository.findById(saved.getId()).orElseThrow();
    assertEquals(EmailStatus.EMAIL_STATUS_SENT, reloaded.getStatus());
    assertNotNull(reloaded.getSentAt());
  }
}
