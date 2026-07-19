package com.coddicted.buzzma.communications.whatsapp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coddicted.buzzma.communications.config.WhatsAppProperties;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class WhatsAppSignatureVerifierTest {

  private static final String APP_SECRET = "test-app-secret";
  private static final String PAYLOAD = "{\"object\":\"whatsapp_business_account\"}";

  private final WhatsAppProperties properties = new WhatsAppProperties();
  private final WhatsAppSignatureVerifier verifier;

  WhatsAppSignatureVerifierTest() {
    this.properties.setAppSecret(APP_SECRET);
    this.verifier = new WhatsAppSignatureVerifier(this.properties);
  }

  @Test
  void acceptsSignatureComputedWithAppSecret() {
    final String signature = "sha256=" + hmacHex(APP_SECRET, PAYLOAD);
    assertTrue(this.verifier.isValid(PAYLOAD, signature));
  }

  @Test
  void rejectsSignatureComputedWithDifferentSecret() {
    final String signature = "sha256=" + hmacHex("wrong-secret", PAYLOAD);
    assertFalse(this.verifier.isValid(PAYLOAD, signature));
  }

  @Test
  void rejectsSignatureForDifferentPayload() {
    final String signature = "sha256=" + hmacHex(APP_SECRET, PAYLOAD);
    assertFalse(this.verifier.isValid("{\"tampered\":true}", signature));
  }

  @Test
  void rejectsMissingSignatureHeader() {
    assertFalse(this.verifier.isValid(PAYLOAD, null));
  }

  @Test
  void rejectsSignatureHeaderWithoutPrefix() {
    final String signature = hmacHex(APP_SECRET, PAYLOAD);
    assertFalse(this.verifier.isValid(PAYLOAD, signature));
  }

  private static String hmacHex(final String secret, final String payload) {
    try {
      final Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }
}
