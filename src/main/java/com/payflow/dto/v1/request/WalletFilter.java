package com.payflow.dto.v1.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

public record WalletFilter(
    @Positive(message = "User ID must be positive")
    Long userId,
    String currency,
    @DateTimeFormat(iso = ISO.DATE)
    @PastOrPresent(message = "From date cannot be in the future")
    LocalDate fromDate,
    @DateTimeFormat(iso = ISO.DATE)
    @PastOrPresent(message = "To date cannot be in the future")
    LocalDate toDate) {
}
