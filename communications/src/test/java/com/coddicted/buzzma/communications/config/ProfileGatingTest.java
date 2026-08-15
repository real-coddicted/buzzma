package com.coddicted.buzzma.communications.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.coddicted.buzzma.communications.email.controller.EmailController;
import com.coddicted.buzzma.communications.email.worker.EmailOutboxWorker;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

/**
 * The {@code api} and {@code worker} instances are meant to run as independent processes (see
 * application-worker.yml, which also disables the web layer for the worker instance). These
 * assertions pin down the {@code @Profile} expressions so a future edit can't silently reintroduce
 * a hard dependency between the two.
 */
class ProfileGatingTest {

  @Test
  void controllerIsExcludedFromTheWorkerProfile() {
    assertArrayEquals(new String[] {"!worker"}, profileValues(EmailController.class));
  }

  @Test
  void securityConfigIsExcludedFromTheWorkerProfile() {
    assertArrayEquals(new String[] {"!worker"}, profileValues(SecurityConfig.class));
  }

  @Test
  void outboxWorkerIsExcludedFromTheApiProfile() {
    assertArrayEquals(new String[] {"!api"}, profileValues(EmailOutboxWorker.class));
  }

  private static String[] profileValues(final Class<?> type) {
    final Profile profile = type.getAnnotation(Profile.class);
    return profile == null ? new String[0] : profile.value();
  }
}
