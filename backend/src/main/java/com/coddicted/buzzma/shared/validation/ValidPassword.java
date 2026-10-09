package com.coddicted.buzzma.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enforces the password policy for creating or changing a password: 8-64 characters, at least one
 * uppercase letter, one lowercase letter, one digit and one special character, and no whitespace.
 * Null values are considered valid; combine with {@code @NotBlank} to require a value.
 */
@Documented
@Constraint(validatedBy = PasswordPolicyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

  String message() default "Password does not meet the password policy";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
