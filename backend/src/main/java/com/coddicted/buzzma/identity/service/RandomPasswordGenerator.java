package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.shared.validation.PasswordPolicyValidator;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

/** Generates random passwords that satisfy {@link PasswordPolicyValidator}. */
@Component
public class RandomPasswordGenerator {

  private static final int LENGTH = PasswordPolicyValidator.MIN_LENGTH;
  private static final String UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ";
  private static final String LOWERCASE = "abcdefghijkmnpqrstuvwxyz";
  private static final String DIGITS = "23456789";
  private static final String SPECIAL = "!@#$%^&*-_=+";
  private static final String ALL = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;

  private static final SecureRandom RANDOM = new SecureRandom();

  public String generate() {
    final List<Character> chars = new ArrayList<>(LENGTH);
    chars.add(randomCharFrom(UPPERCASE));
    chars.add(randomCharFrom(LOWERCASE));
    chars.add(randomCharFrom(DIGITS));
    chars.add(randomCharFrom(SPECIAL));
    for (int i = chars.size(); i < LENGTH; i++) {
      chars.add(randomCharFrom(ALL));
    }
    Collections.shuffle(chars, RANDOM);

    final StringBuilder password = new StringBuilder(LENGTH);
    chars.forEach(password::append);
    return password.toString();
  }

  private char randomCharFrom(final String pool) {
    return pool.charAt(RANDOM.nextInt(pool.length()));
  }
}
