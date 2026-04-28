package com.coddicted.buzzma.shared.common;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class CodeGenerator {

  private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final int CODE_LENGTH = 6;
  private static final SecureRandom RANDOM = new SecureRandom();

  public String generateHumanCode(String prefix) {
    StringBuilder sb = new StringBuilder(prefix).append('-');
    for (int i = 0; i < CODE_LENGTH; i++) {
      sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
    }
    return sb.toString();
  }
}
