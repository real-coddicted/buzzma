package com.coddicted.buzzma.communications.email.service;

import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.FAILED_LOG;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.FROM_ADDRESS;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.PENDING_LOG;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.REQUESTED_BY;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.REQUEST_ID;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.SENT_LOG;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.SUBJECT;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.TO_ADDRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.communications.email.model.EmailCommunicationLog;
import com.coddicted.buzzma.communications.email.model.EmailStatus;
import com.coddicted.buzzma.communications.email.repository.EmailCommunicationLogRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailCommunicationLogServiceTest {

  @Mock private EmailCommunicationLogRepository mockRepository;
  private EmailCommunicationLogService service;

  @BeforeEach
  void setUp() {
    this.service = new EmailCommunicationLogService(this.mockRepository);
  }

  @Test
  void createPendingPersistsANewPendingLog() {
    when(this.mockRepository.save(any(EmailCommunicationLog.class))).thenReturn(PENDING_LOG);

    final EmailCommunicationLog result =
        this.service.createPending(TO_ADDRESS, FROM_ADDRESS, SUBJECT, REQUESTED_BY);

    assertEquals(PENDING_LOG, result);

    final ArgumentCaptor<EmailCommunicationLog> captor =
        ArgumentCaptor.forClass(EmailCommunicationLog.class);
    verify(this.mockRepository).save(captor.capture());
    final EmailCommunicationLog saved = captor.getValue();
    assertEquals(TO_ADDRESS, saved.getToAddress());
    assertEquals(FROM_ADDRESS, saved.getFromAddress());
    assertEquals(SUBJECT, saved.getSubject());
    assertEquals(EmailStatus.EMAIL_STATUS_PENDING, saved.getStatus());
    assertEquals(REQUESTED_BY, saved.getCreatedBy());
    assertEquals(REQUESTED_BY, saved.getUpdatedBy());
  }

  @Test
  void markSentUpdatesStatusAndSentAt() {
    when(this.mockRepository.save(PENDING_LOG)).thenReturn(SENT_LOG);

    final EmailCommunicationLog result = this.service.markSent(PENDING_LOG);

    assertEquals(SENT_LOG, result);
    assertEquals(EmailStatus.EMAIL_STATUS_SENT, PENDING_LOG.getStatus());
    assertEquals(REQUEST_ID, PENDING_LOG.getId());
    assertTrue(PENDING_LOG.getSentAt() != null);
  }

  @Test
  void markFailedUpdatesStatusAndErrorMessage() {
    when(this.mockRepository.save(PENDING_LOG)).thenReturn(FAILED_LOG);

    final EmailCommunicationLog result = this.service.markFailed(PENDING_LOG, "smtp down");

    assertEquals(FAILED_LOG, result);
    assertEquals(EmailStatus.EMAIL_STATUS_FAILED, PENDING_LOG.getStatus());
    assertEquals("smtp down", PENDING_LOG.getErrorMessage());
  }

  @Test
  void findByIdReturnsEmptyWhenNotFound() {
    when(this.mockRepository.findById(REQUEST_ID)).thenReturn(Optional.empty());

    final Optional<EmailCommunicationLog> result = this.service.findById(REQUEST_ID);

    assertTrue(result.isEmpty());
  }

  @Test
  void findByIdReturnsLogWhenFound() {
    when(this.mockRepository.findById(REQUEST_ID)).thenReturn(Optional.of(SENT_LOG));

    final Optional<EmailCommunicationLog> result = this.service.findById(REQUEST_ID);

    assertEquals(SENT_LOG, result.orElseThrow());
  }
}
