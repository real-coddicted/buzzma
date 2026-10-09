package com.coddicted.buzzma.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Validates {@link ValidPassword}. Reports the first failed rule as the violation message. Keep in
 * sync with frontend/src/utils/passwordPolicy.ts.
 */
public class PasswordPolicyValidator implements ConstraintValidator<ValidPassword, String> {

  public static final int MIN_LENGTH = 8;
  public static final int MAX_LENGTH = 64;

  private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
  private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
  private static final Pattern DIGIT = Pattern.compile("[0-9]");
  private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9\\s]");
  private static final Pattern WHITESPACE = Pattern.compile("\\s");

  private record Rule(Predicate<String> passes, String message) {}

  private static final List<Rule> RULES =
      List.of(
          new Rule(
              p -> p.length() >= MIN_LENGTH,
              "Password must be at least " + MIN_LENGTH + " characters"),
          new Rule(
              p -> p.length() <= MAX_LENGTH,
              "Password must be at most " + MAX_LENGTH + " characters"),
          new Rule(
              p -> UPPERCASE.matcher(p).find(),
              "Password must contain at least one uppercase letter"),
          new Rule(
              p -> LOWERCASE.matcher(p).find(),
              "Password must contain at least one lowercase letter"),
          new Rule(p -> DIGIT.matcher(p).find(), "Password must contain at least one digit"),
          new Rule(
              p -> SPECIAL.matcher(p).find(),
              "Password must contain at least one special character"),
          new Rule(p -> !WHITESPACE.matcher(p).find(), "Password must not contain spaces"));

  @Override
  public boolean isValid(final String password, final ConstraintValidatorContext context) {
    if (password == null) {
      return true;
    }
    for (final Rule rule : RULES) {
      if (!rule.passes().test(password)) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(rule.message()).addConstraintViolation();
        return false;
      }
    }
    return true;
  }
}
