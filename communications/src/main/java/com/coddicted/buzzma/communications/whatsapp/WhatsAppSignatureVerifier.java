package com.coddicted.buzzma.communications.whatsapp;

import com.coddicted.buzzma.communications.config.WhatsAppProperties;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class WhatsAppSignatureVerifier {

  private static final String HMAC_ALGORITHM = "HmacSHA256";
  private static final String SIGNATURE_PREFIX = "sha256=";

  private final WhatsAppProperties properties;

  public WhatsAppSignatureVerifier(final WhatsAppProperties properties) {
    this.properties = properties;
  }

  public boolean isValid(final String payload, final String signatureHeader) {
    if (signatureHeader == null || !signatureHeader.startsWith(SIGNATURE_PREFIX)) {
      return false;
    }
    final String providedHex = signatureHeader.substring(SIGNATURE_PREFIX.length());
    final String computedHex = computeHmacHex(payload);
    return MessageDigest.isEqual(
        computedHex.getBytes(StandardCharsets.UTF_8), providedHex.getBytes(StandardCharsets.UTF_8));
  }

  private String computeHmacHex(final String payload) {
    try {
      final Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(
          new SecretKeySpec(
              this.properties.getAppSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
      return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Unable to compute WhatsApp webhook HMAC", e);
    }
  }
}
