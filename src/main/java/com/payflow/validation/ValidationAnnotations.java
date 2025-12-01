package com.payflow.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotations for DTOs
 * Define all validation rules in one place for easy maintenance
 * This is a utility class and should not be instantiated.
 */
public final class ValidationAnnotations {

    /**
     * Private constructor to prevent instantiation
     */
    private ValidationAnnotations() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Validates password: 8-20 characters, not blank
     */
    @Constraint(validatedBy = PasswordValidator.class)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ValidPassword {
        String message() default "Password must be between 8 and 20 characters";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /**
     * Validates email: valid format, not blank
     */
    @Constraint(validatedBy = EmailValidator.class)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ValidEmail {
        String message() default "Email must be valid";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    /**
     * Validates full name: 2-100 characters, not blank
     */
    @Constraint(validatedBy = FullNameValidator.class)
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ValidFullName {
        String message() default "Full name must be between 2 and 100 characters";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

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
