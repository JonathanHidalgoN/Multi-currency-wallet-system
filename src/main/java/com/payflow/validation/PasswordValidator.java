package com.payflow.validation;

import com.payflow.validation.ValidationAnnotations.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for @ValidPassword annotation
 * Checks if password is not blank and between 8-20 characters
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    // Null or blank passwords are invalid
    if (password == null || password.isBlank()) {
      return false;
    }

    // Check length: must be between 8 and 20 characters
    return password.length() >= 8 && password.length() <= 20;
  }
}
