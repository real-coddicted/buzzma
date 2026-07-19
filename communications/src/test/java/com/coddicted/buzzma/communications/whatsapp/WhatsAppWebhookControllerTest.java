package com.coddicted.buzzma.communications.whatsapp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.coddicted.buzzma.communications.config.WhatsAppProperties;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class WhatsAppWebhookControllerTest {

  private static final String VERIFY_TOKEN = "test-verify-token";
  private static final String APP_SECRET = "test-app-secret";

  private final WhatsAppProperties properties = new WhatsAppProperties();
  private final WhatsAppWebhookController controller;

  WhatsAppWebhookControllerTest() {
    this.properties.setVerifyToken(VERIFY_TOKEN);
    this.properties.setAppSecret(APP_SECRET);
    this.controller =
        new WhatsAppWebhookController(
            this.properties, new WhatsAppSignatureVerifier(this.properties));
  }

  @Test
  void echoesChallengeWhenModeAndTokenMatch() {
    final ResponseEntity<String> response =
        this.controller.verify("subscribe", VERIFY_TOKEN, "1234567890");
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("1234567890", response.getBody());
  }

  @Test
  void rejectsVerificationWithWrongToken() {
    final ResponseEntity<String> response =
        this.controller.verify("subscribe", "wrong-token", "1234567890");
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void rejectsVerificationWithWrongMode() {
    final ResponseEntity<String> response =
        this.controller.verify("unsubscribe", VERIFY_TOKEN, "1234567890");
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void acceptsPayloadWithValidSignature() {
    final String payload = "{\"object\":\"whatsapp_business_account\"}";
    final String signature = "sha256=" + hmacHex(payload);
    final ResponseEntity<Void> response = this.controller.receive(signature, payload);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void rejectsPayloadWithInvalidSignature() {
    final String payload = "{\"object\":\"whatsapp_business_account\"}";
    final ResponseEntity<Void> response = this.controller.receive("sha256=invalid", payload);
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void rejectsPayloadWithMissingSignature() {
    final String payload = "{\"object\":\"whatsapp_business_account\"}";
    final ResponseEntity<Void> response = this.controller.receive(null, payload);
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  private String hmacHex(final String payload) {
    try {
      final Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(APP_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }
}
