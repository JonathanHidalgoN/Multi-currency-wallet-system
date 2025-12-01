package com.payflow.validation;

import com.payflow.validation.ValidationAnnotations.ValidFullName;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for @ValidFullName annotation
 * Checks if full name is not blank and between 2-100 characters
 */
public class FullNameValidator implements ConstraintValidator<ValidFullName, String> {

  @Override
  public boolean isValid(String fullName, ConstraintValidatorContext context) {
    if (fullName == null || fullName.isBlank()) {
      return false;
    }

    return fullName.length() >= 2 && fullName.length() <= 100;
  }
}
