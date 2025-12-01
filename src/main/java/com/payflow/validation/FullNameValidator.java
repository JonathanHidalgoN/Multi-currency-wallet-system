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
    // Null or blank names are invalid
    if (fullName == null || fullName.isBlank()) {
      return false;
    }

    // Check length: must be between 2 and 100 characters
    return fullName.length() >= 2 && fullName.length() <= 100;
  }
}
