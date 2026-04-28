package com.coddicted.buzzma.shared.controller;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

  private final UserQueryPort userQueryPort;

  public HealthController(UserQueryPort userQueryPort) {
    this.userQueryPort = userQueryPort;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> health() {
    Map<String, Object> body = new HashMap<>();
    body.put("status", "ok");
    long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
    body.put("uptime", uptimeMs / 1000.0);

    try {
      userQueryPort.count();
      body.put("db", "up");
      return ResponseEntity.ok(body);
    } catch (Exception e) {
      body.put("db", "down");
      body.put("status", "degraded");
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
  }
}
