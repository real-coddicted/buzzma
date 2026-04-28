package com.coddicted.buzzma.shared.controller;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/realtime")
public class RealtimeController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RealtimeController.class);
  private static final long EMITTER_TIMEOUT_MS = 30 * 60 * 1000L;

  @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @PreAuthorize("isAuthenticated()")
  public SseEmitter stream() {
    SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
    try {
      emitter.send(SseEmitter.event().comment("connected"));
    } catch (IOException ex) {
      LOGGER.debug("SSE client disconnected before initial comment: {}", ex.getMessage());
      emitter.completeWithError(ex);
      return emitter;
    }
    emitter.onTimeout(emitter::complete);
    emitter.onError(err -> emitter.complete());
    return emitter;
  }
}
