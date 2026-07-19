package com.coddicted.buzzma.communications.whatsapp;

import com.coddicted.buzzma.communications.config.WhatsAppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks/whatsapp")
public class WhatsAppWebhookController {

  private static final Logger LOGGER = LoggerFactory.getLogger(WhatsAppWebhookController.class);
  private static final String SIGNATURE_HEADER = "X-Hub-Signature-256";

  private final WhatsAppProperties properties;
  private final WhatsAppSignatureVerifier signatureVerifier;

  public WhatsAppWebhookController(
      final WhatsAppProperties properties, final WhatsAppSignatureVerifier signatureVerifier) {
    this.properties = properties;
    this.signatureVerifier = signatureVerifier;
  }

  @GetMapping
  public ResponseEntity<String> verify(
      @RequestParam("hub.mode") final String mode,
      @RequestParam("hub.verify_token") final String verifyToken,
      @RequestParam("hub.challenge") final String challenge) {
    if ("subscribe".equals(mode) && this.properties.getVerifyToken().equals(verifyToken)) {
      return ResponseEntity.ok(challenge);
    }
    LOGGER.warn("Rejected WhatsApp webhook verification attempt: mode={}", mode);
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  @PostMapping
  public ResponseEntity<Void> receive(
      @RequestHeader(value = SIGNATURE_HEADER, required = false) final String signature,
      @RequestBody final String payload) {
    if (!this.signatureVerifier.isValid(payload, signature)) {
      LOGGER.warn("Rejected WhatsApp webhook payload with invalid signature");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    LOGGER.info("Received WhatsApp webhook event: {}", payload);
    return ResponseEntity.ok().build();
  }
}
