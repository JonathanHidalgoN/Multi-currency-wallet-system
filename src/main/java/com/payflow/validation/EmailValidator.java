package com.payflow.validation;

import com.payflow.validation.ValidationAnnotations.ValidEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for @ValidEmail annotation
 * Checks if email is not blank and has valid email format
 */
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

  // Simple email regex pattern
  private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

  @Override
  public boolean isValid(String email, ConstraintValidatorContext context) {
    // Null or blank emails are invalid
    if (email == null || email.isBlank()) {
      return false;
    }

    // Check if matches email pattern
    return email.matches(EMAIL_PATTERN);
  }
}
