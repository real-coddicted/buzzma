package com.coddicted.buzzma.communications.email.worker;

import com.coddicted.buzzma.communications.email.client.EmailClient;
import com.coddicted.buzzma.communications.email.config.EmailOutboxProperties;
import com.coddicted.buzzma.communications.email.constant.WellKnownQueues;
import com.coddicted.buzzma.communications.email.model.EmailCommunicationLog;
import com.coddicted.buzzma.communications.email.model.EmailOutboxMessage;
import com.coddicted.buzzma.communications.email.service.EmailCommunicationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;

/** Excluded from the {@code api} profile — that instance only serves HTTP, no background work. */
@Component
@Profile("!api")
public class EmailOutboxWorker {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmailOutboxWorker.class);
  private static final Duration POP_TIMEOUT = Duration.ofSeconds(5);

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private final EmailClient emailClient;
  private final EmailCommunicationLogService logService;
  private final EmailOutboxProperties properties;

  private ExecutorService executorService;
  private volatile boolean running;

  public EmailOutboxWorker(
      final StringRedisTemplate redisTemplate,
      final ObjectMapper objectMapper,
      final EmailClient emailClient,
      final EmailCommunicationLogService logService,
      final EmailOutboxProperties properties) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.emailClient = emailClient;
    this.logService = logService;
    this.properties = properties;
  }

  @PostConstruct
  void start() {
    this.running = true;
    final int poolSize = this.properties.getWorkerPoolSize();
    this.executorService = Executors.newFixedThreadPool(poolSize);
    for (int i = 0; i < poolSize; i++) {
      this.executorService.submit(this::runLoop);
    }
  }

  @PreDestroy
  void stop() {
    this.running = false;
    if (this.executorService != null) {
      this.executorService.shutdownNow();
    }
  }

  private void runLoop() {
    while (this.running) {
      try {
        processOne();
      } catch (final RuntimeException ex) {
        LOGGER.warn("Email outbox worker failed to process message: {}", ex.getMessage());
      }
    }
  }

  /** Pops and processes a single message, if one is available within the poll timeout. */
  void processOne() {
    final String json =
        this.redisTemplate
            .opsForList()
            .rightPop(WellKnownQueues.EMAIL_OUTBOX_QUEUE_KEY, POP_TIMEOUT);
    if (json == null) {
      return;
    }
    final EmailOutboxMessage message = readMessage(json);
    this.logService
        .findById(message.getRequestId())
        .ifPresentOrElse(
            log -> sendAndUpdateStatus(log, message),
            () ->
                LOGGER.warn(
                    "No email_communication_log row found for requestId={}",
                    message.getRequestId()));
  }

  private EmailOutboxMessage readMessage(final String json) {
    try {
      return this.objectMapper.readValue(json, EmailOutboxMessage.class);
    } catch (final Exception ex) {
      throw new IllegalStateException("Failed to deserialize email outbox message", ex);
    }
  }

  private void sendAndUpdateStatus(
      final EmailCommunicationLog log, final EmailOutboxMessage message) {
    try {
      this.emailClient.send(message.getTo(), message.getSubject(), message.getBody());
      this.logService.markSent(log);
    } catch (final MailException ex) {
      LOGGER.warn("Failed to send email for requestId={}: {}", log.getId(), ex.getMessage());
      this.logService.markFailed(log, ex.getMessage());
    }
  }
}
