package com.coddicted.buzzma.identity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coddicted.buzzma.shared.validation.PasswordPolicyValidator;
import com.coddicted.buzzma.shared.validation.ValidPassword;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class RandomPasswordGeneratorTest {

  private record PasswordHolder(@ValidPassword String password) {}

  private final RandomPasswordGenerator generator = new RandomPasswordGenerator();

  @RepeatedTest(100)
  void generatedPasswordSatisfiesPasswordPolicy() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      final Validator validator = factory.getValidator();
      final String password = generator.generate();

      assertTrue(
          validator.validate(new PasswordHolder(password)).isEmpty(),
          "generated password violates policy: " + password);
    }
  }

  @Test
  void generatedPasswordHasMinimumLength() {
    assertEquals(PasswordPolicyValidator.MIN_LENGTH, generator.generate().length());
  }
}
