package com.coddicted.buzzma.identity.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class OtpGenerator {

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final int OTP_BOUND = 1_000_000;

  public String generate() {
    return String.format("%06d", RANDOM.nextInt(OTP_BOUND));
  }
}
