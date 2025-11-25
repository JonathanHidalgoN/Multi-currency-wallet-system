package com.payflow.validation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotations for DTOs
 * Define all validation rules in one place for easy maintenance
 */
public class ValidationAnnotations {

    /**
     * Validates password: 8-20 characters, not blank
     */
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ValidPassword {}

    /**
     * Validates email: valid format, not blank
     */
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ValidEmail {}

    /**
     * Validates full name: 2-100 characters, not blank
     */
    @NotBlank(message = "Full name cannot be blank")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ValidFullName {}

    /**
     * Validates currency code: 3-letter ISO 4217 code (USD, EUR, GBP, etc.)
     */
    @NotBlank(message = "Currency cannot be blank")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO 4217 code (e.g., USD, EUR)")
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ValidCurrencyCode {}

    /**
     * Validates amount: positive, minimum 0.01, not null
     */
    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ValidAmount {}
}
