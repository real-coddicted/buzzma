package com.coddicted.buzzma.communications.email.controller;

import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.BODY;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.FAILED_LOG;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.FROM_ADDRESS;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.PENDING_LOG;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.REQUEST_ID;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.SENT_LOG;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.SUBJECT;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.TO_ADDRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.communications.email.config.EmailProperties;
import com.coddicted.buzzma.communications.email.dto.EmailStatusResponseDto;
import com.coddicted.buzzma.communications.email.dto.SendEmailRequestDto;
import com.coddicted.buzzma.communications.email.dto.SendEmailResponseDto;
import com.coddicted.buzzma.communications.email.mapper.EmailCommunicationLogMapperImpl;
import com.coddicted.buzzma.communications.email.model.EmailStatus;
import com.coddicted.buzzma.communications.email.publisher.EmailOutboxPublisher;
import com.coddicted.buzzma.communications.email.service.EmailCommunicationLogService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class EmailControllerTest {

  @Mock private EmailCommunicationLogService mockLogService;
  @Mock private EmailOutboxPublisher mockOutboxPublisher;
  private EmailController controller;

  @BeforeEach
  void setUp() {
    final EmailProperties properties = new EmailProperties();
    properties.setFromAddress(FROM_ADDRESS);
    this.controller =
        new EmailController(
            properties,
            this.mockLogService,
            new EmailCommunicationLogMapperImpl(),
            this.mockOutboxPublisher);
  }

  @Test
  void acceptsRequestAndPublishesToTheOutbox() {
    when(this.mockLogService.createPending(TO_ADDRESS, FROM_ADDRESS, SUBJECT, null))
        .thenReturn(PENDING_LOG);

    final ResponseEntity<SendEmailResponseDto> response =
        this.controller.send(
            SendEmailRequestDto.builder().to(TO_ADDRESS).subject(SUBJECT).body(BODY).build());

    assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    assertEquals(REQUEST_ID, response.getBody().getRequestId());
    assertEquals(EmailStatus.EMAIL_STATUS_PENDING, response.getBody().getStatus());
    verify(this.mockOutboxPublisher).publish(REQUEST_ID, TO_ADDRESS, SUBJECT, BODY);
  }

  @Test
  void marksLogFailedAndReturnsServiceUnavailableWhenPublishThrows() {
    when(this.mockLogService.createPending(TO_ADDRESS, FROM_ADDRESS, SUBJECT, null))
        .thenReturn(PENDING_LOG);
    doThrow(new RedisConnectionFailureException("redis down"))
        .when(this.mockOutboxPublisher)
        .publish(REQUEST_ID, TO_ADDRESS, SUBJECT, BODY);
    when(this.mockLogService.markFailed(PENDING_LOG, "redis down")).thenReturn(FAILED_LOG);

    final ResponseEntity<SendEmailResponseDto> response =
        this.controller.send(
            SendEmailRequestDto.builder().to(TO_ADDRESS).subject(SUBJECT).body(BODY).build());

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    assertEquals(EmailStatus.EMAIL_STATUS_FAILED, response.getBody().getStatus());
    assertEquals("5", response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER));
    verify(this.mockLogService).markFailed(PENDING_LOG, "redis down");
  }

  @Test
  void returnsNotFoundForUnknownRequestId() {
    final UUID unknownId = UUID.randomUUID();
    when(this.mockLogService.findById(unknownId)).thenReturn(Optional.empty());

    final ResponseEntity<EmailStatusResponseDto> response = this.controller.getStatus(unknownId);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void returnsStatusForKnownRequestId() {
    when(this.mockLogService.findById(REQUEST_ID)).thenReturn(Optional.of(SENT_LOG));

    final ResponseEntity<EmailStatusResponseDto> response = this.controller.getStatus(REQUEST_ID);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(TO_ADDRESS, response.getBody().getTo());
    assertEquals(EmailStatus.EMAIL_STATUS_SENT, response.getBody().getStatus());
  }
}
