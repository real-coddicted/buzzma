package com.coddicted.buzzma.communications.email.worker;

import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.BODY;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.PENDING_LOG;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.REQUEST_ID;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.SUBJECT;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.TO_ADDRESS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.communications.email.client.EmailClient;
import com.coddicted.buzzma.communications.email.config.EmailOutboxProperties;
import com.coddicted.buzzma.communications.email.constant.WellKnownQueues;
import com.coddicted.buzzma.communications.email.model.EmailOutboxMessage;
import com.coddicted.buzzma.communications.email.service.EmailCommunicationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailSendException;

@ExtendWith(MockitoExtension.class)
class EmailOutboxWorkerTest {

  @Mock private StringRedisTemplate mockRedisTemplate;
  @Mock private EmailClient mockEmailClient;
  @Mock private EmailCommunicationLogService mockLogService;

  @SuppressWarnings("unchecked")
  private final ListOperations<String, String> mockListOperations = mock(ListOperations.class);

  private final ObjectMapper objectMapper = new ObjectMapper();
  private EmailOutboxWorker worker;

  @BeforeEach
  void setUp() {
    when(this.mockRedisTemplate.opsForList()).thenReturn(this.mockListOperations);
    this.worker =
        new EmailOutboxWorker(
            this.mockRedisTemplate,
            this.objectMapper,
            this.mockEmailClient,
            this.mockLogService,
            new EmailOutboxProperties());
  }

  @Test
  void doesNothingWhenQueueIsEmpty() {
    when(this.mockListOperations.rightPop(
            WellKnownQueues.EMAIL_OUTBOX_QUEUE_KEY, Duration.ofSeconds(5)))
        .thenReturn(null);

    this.worker.processOne();

    verifyNoInteractions(this.mockEmailClient);
  }

  @Test
  void sendsEmailAndMarksSentOnSuccess() throws Exception {
    when(this.mockListOperations.rightPop(
            WellKnownQueues.EMAIL_OUTBOX_QUEUE_KEY, Duration.ofSeconds(5)))
        .thenReturn(toJson(REQUEST_ID, TO_ADDRESS, SUBJECT, BODY));
    when(this.mockLogService.findById(REQUEST_ID)).thenReturn(Optional.of(PENDING_LOG));
    doNothing().when(this.mockEmailClient).send(TO_ADDRESS, SUBJECT, BODY);

    this.worker.processOne();

    verify(this.mockEmailClient).send(TO_ADDRESS, SUBJECT, BODY);
    verify(this.mockLogService).markSent(PENDING_LOG);
  }

  @Test
  void marksFailedWhenEmailClientThrows() throws Exception {
    when(this.mockListOperations.rightPop(
            WellKnownQueues.EMAIL_OUTBOX_QUEUE_KEY, Duration.ofSeconds(5)))
        .thenReturn(toJson(REQUEST_ID, TO_ADDRESS, SUBJECT, BODY));
    when(this.mockLogService.findById(REQUEST_ID)).thenReturn(Optional.of(PENDING_LOG));
    doThrow(new MailSendException("smtp down"))
        .when(this.mockEmailClient)
        .send(TO_ADDRESS, SUBJECT, BODY);

    this.worker.processOne();

    verify(this.mockLogService).markFailed(PENDING_LOG, "smtp down");
  }

  @Test
  void skipsSendingWhenLogRowIsMissing() throws Exception {
    final UUID unknownRequestId = UUID.randomUUID();
    when(this.mockListOperations.rightPop(
            WellKnownQueues.EMAIL_OUTBOX_QUEUE_KEY, Duration.ofSeconds(5)))
        .thenReturn(toJson(unknownRequestId, TO_ADDRESS, SUBJECT, BODY));
    when(this.mockLogService.findById(unknownRequestId)).thenReturn(Optional.empty());

    this.worker.processOne();

    verifyNoInteractions(this.mockEmailClient);
  }

  private String toJson(
      final UUID requestId, final String to, final String subject, final String body)
      throws Exception {
    final EmailOutboxMessage message =
        EmailOutboxMessage.builder()
            .requestId(requestId)
            .to(to)
            .subject(subject)
            .body(body)
            .build();
    return this.objectMapper.writeValueAsString(message);
  }
}
