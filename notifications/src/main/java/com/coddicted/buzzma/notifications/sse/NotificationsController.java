package com.coddicted.buzzma.notifications.sse;

import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/events")
public class NotificationsController {

  private final NotificationsStreamService streamService;
  private final Duration heartbeatInterval;

  public NotificationsController(
      final NotificationsStreamService streamService,
      @Value("${app.sse.heartbeat-interval-seconds:15}") final long heartbeatSeconds) {
    this.streamService = streamService;
    this.heartbeatInterval = Duration.ofSeconds(heartbeatSeconds);
  }

  @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<?>> stream(@AuthenticationPrincipal final UUID userId) {
    final Flux<ServerSentEvent<?>> events =
        this.streamService
            .streamFor(userId)
            .map(
                event ->
                    (ServerSentEvent<?>)
                        ServerSentEvent.builder(event)
                            .id(event.getId())
                            .event(event.getType())
                            .build());

    final Flux<ServerSentEvent<?>> heartbeats =
        Flux.interval(this.heartbeatInterval, this.heartbeatInterval)
            .map(tick -> ServerSentEvent.builder().comment("keep-alive").build());

    return Flux.merge(events, heartbeats);
  }
}
