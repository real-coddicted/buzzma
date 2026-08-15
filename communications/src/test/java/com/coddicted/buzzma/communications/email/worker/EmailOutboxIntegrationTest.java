package com.coddicted.buzzma.communications.email.worker;

import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.BODY;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.PENDING_LOG;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.REQUEST_ID;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.SUBJECT;
import static com.coddicted.buzzma.communications.testsupport.EmailLogFixtures.TO_ADDRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.communications.email.client.EmailClient;
import com.coddicted.buzzma.communications.email.config.EmailOutboxProperties;
import com.coddicted.buzzma.communications.email.constant.WellKnownQueues;
import com.coddicted.buzzma.communications.email.publisher.EmailOutboxPublisher;
import com.coddicted.buzzma.communications.email.service.EmailCommunicationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Exercises {@link EmailOutboxPublisher} and {@link EmailOutboxWorker} against a real Redis
 * instance to prove the queue key and message format actually line up end-to-end, not just against
 * mocks.
 */
@Testcontainers
class EmailOutboxIntegrationTest {

  private static final GenericContainer<?> REDIS =
      new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

  private static LettuceConnectionFactory connectionFactory;

  @BeforeAll
  static void startRedis() {
    REDIS.start();
    connectionFactory =
        new LettuceConnectionFactory(
            new RedisStandaloneConfiguration(REDIS.getHost(), REDIS.getMappedPort(6379)));
    connectionFactory.afterPropertiesSet();
  }

  @AfterAll
  static void stopRedis() {
    if (connectionFactory != null) {
      connectionFactory.destroy();
    }
    REDIS.stop();
  }

  @Test
  void publishedMessageIsConsumedAndEmailIsSent() throws Exception {
    final StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory);
    redisTemplate.afterPropertiesSet();
    final ObjectMapper objectMapper = new ObjectMapper();

    final EmailOutboxPublisher publisher = new EmailOutboxPublisher(redisTemplate, objectMapper);

    final EmailClient emailClient = mock(EmailClient.class);
    final EmailCommunicationLogService logService = mock(EmailCommunicationLogService.class);
    when(logService.findById(REQUEST_ID)).thenReturn(Optional.of(PENDING_LOG));
    doNothing().when(emailClient).send(TO_ADDRESS, SUBJECT, BODY);

    final EmailOutboxWorker worker =
        new EmailOutboxWorker(
            redisTemplate, objectMapper, emailClient, logService, new EmailOutboxProperties());

    publisher.publish(REQUEST_ID, TO_ADDRESS, SUBJECT, BODY);
    worker.processOne();

    verify(emailClient).send(TO_ADDRESS, SUBJECT, BODY);
    verify(logService).markSent(PENDING_LOG);
    assertEquals(0L, redisTemplate.opsForList().size(WellKnownQueues.EMAIL_OUTBOX_QUEUE_KEY));
  }
}
