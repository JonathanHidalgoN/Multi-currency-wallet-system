package com.payflow.DTOS;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO for transfer requests
 */
public record TransferRequest(
    @NotNull(message = "Recipient user ID cannot be null")
    Long recipientUserId,

    @NotBlank(message = "Sender currency cannot be blank")
    String senderCurrency,

    @NotBlank(message = "Recipient currency cannot be blank")
    String recipientCurrency,

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    BigDecimal amount
) {}
