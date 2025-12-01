package com.payflow.validation;

import com.payflow.validation.ValidationAnnotations.ValidEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for @ValidEmail annotation
 * Checks if email is not blank and has valid email format
 */
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

  private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

  @Override
  public boolean isValid(String email, ConstraintValidatorContext context) {
    if (email == null || email.isBlank()) {
      return false;
    }

    return email.matches(EMAIL_PATTERN);
  }
}
