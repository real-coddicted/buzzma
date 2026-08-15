package com.coddicted.buzzma.communications.email.publisher;

import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.BODY;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.REQUEST_ID;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.SUBJECT;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.TO_ADDRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.communications.email.constant.WellKnownQueues;
import com.coddicted.buzzma.communications.email.model.EmailOutboxMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class EmailOutboxPublisherTest {

  @Mock private StringRedisTemplate mockRedisTemplate;

  @SuppressWarnings("unchecked")
  private final ListOperations<String, String> mockListOperations = mock(ListOperations.class);

  private final ObjectMapper objectMapper = new ObjectMapper();
  private EmailOutboxPublisher publisher;

  @BeforeEach
  void setUp() {
    when(this.mockRedisTemplate.opsForList()).thenReturn(this.mockListOperations);
    this.publisher = new EmailOutboxPublisher(this.mockRedisTemplate, this.objectMapper);
  }

  @Test
  void pushesSerializedMessageOntoTheQueue() throws Exception {
    this.publisher.publish(REQUEST_ID, TO_ADDRESS, SUBJECT, BODY);

    final ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
    verify(this.mockListOperations)
        .leftPush(eq(WellKnownQueues.EMAIL_OUTBOX_QUEUE_KEY), jsonCaptor.capture());

    final EmailOutboxMessage message =
        this.objectMapper.readValue(jsonCaptor.getValue(), EmailOutboxMessage.class);
    assertEquals(REQUEST_ID, message.getRequestId());
    assertEquals(TO_ADDRESS, message.getTo());
    assertEquals(SUBJECT, message.getSubject());
    assertEquals(BODY, message.getBody());
  }
}
