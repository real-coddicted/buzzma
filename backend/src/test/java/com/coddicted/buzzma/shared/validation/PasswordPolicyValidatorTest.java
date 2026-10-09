package com.coddicted.buzzma.shared.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordPolicyValidatorTest {

  private static ValidatorFactory validatorFactory;
  private static Validator validator;

  private record PasswordHolder(@ValidPassword String password) {}

  @BeforeAll
  static void setUp() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  static void tearDown() {
    validatorFactory.close();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Password@123",
        "Abcdef1!",
        "Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#"
      })
  void testValidPasswordHasNoViolations(final String password) {
    assertTrue(validator.validate(new PasswordHolder(password)).isEmpty());
  }

  @Test
  void testNullPasswordHasNoViolations() {
    assertTrue(validator.validate(new PasswordHolder(null)).isEmpty());
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "Ab1!xyz          | Password must be at least 8 characters",
        "Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#Zz9#A"
            + " | Password must be at most 64 characters",
        "password@123     | Password must contain at least one uppercase letter",
        "PASSWORD@123     | Password must contain at least one lowercase letter",
        "Password@abc     | Password must contain at least one digit",
        "Password1234     | Password must contain at least one special character",
        "'Pass word@123'  | Password must not contain spaces"
      })
  void testInvalidPasswordReportsFirstFailedRule(final String password, final String message) {
    final Set<ConstraintViolation<PasswordHolder>> violations =
        validator.validate(new PasswordHolder(password));

    assertEquals(1, violations.size());
    assertEquals(message, violations.iterator().next().getMessage());
  }
}
