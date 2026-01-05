package com.payflow.dto.v1.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.payflow.entity.Transaction;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

public record TransactionFilter(
    String currency,
    Transaction.TransactionType type,
    Transaction.TransactionStatus status,
    @DateTimeFormat(iso = ISO.DATE)
    @PastOrPresent(message = "From date cannot be in the future")
    LocalDate fromDate,
    @DateTimeFormat(iso = ISO.DATE)
    @PastOrPresent(message = "To date cannot be in the future")
    LocalDate toDate,
    @PositiveOrZero(message = "Minimum amount must be zero or positive")
    BigDecimal minAmount,
    @PositiveOrZero(message = "Maximum amount must be zero or positive")
    BigDecimal maxAmount) {
}
