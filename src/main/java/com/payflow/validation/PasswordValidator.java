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
    if (password == null || password.isBlank()) {
      return false;
    }

    return password.length() >= 8 && password.length() <= 20;
  }
}
