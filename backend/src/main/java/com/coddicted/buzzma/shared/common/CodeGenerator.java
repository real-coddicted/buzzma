package com.coddicted.buzzma.shared.common;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/**
 * Generates human-readable code, prefixed with a specified string, followed by a random combination
 * of uppercase letters and digits. The generated code is 8 characters long (excluding the prefix
 * and hyphen) and is designed to be easily distinguishable and memorable, avoiding characters that
 * can be easily confused (like 'I', 'O', '1', and '0'). Example output: "INVITE-ABCDEFGH" or
 * "USER-23456789".
 *
 * <p>The generated human-readable code is not guaranteed to be unique, so it is caller
 * responsibility to ensure uniqueness if required (e.g., by checking against a database before
 * finalizing the code).
 */
@Component
public class CodeGenerator {

  private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final int CODE_LENGTH = 8;
  private static final SecureRandom RANDOM = new SecureRandom();

  public String generateHumanCode(String prefix) {
    StringBuilder sb = new StringBuilder(prefix).append('-');
    for (int i = 0; i < CODE_LENGTH; i++) {
      sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
    }
    return sb.toString();
  }
}
