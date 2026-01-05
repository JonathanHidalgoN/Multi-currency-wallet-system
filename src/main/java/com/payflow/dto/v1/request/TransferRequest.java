package com.payflow.dto.v1.request;

import com.payflow.validation.ValidationAnnotations.ValidAmount;
import com.payflow.validation.ValidationAnnotations.ValidCurrencyCode;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO for transfer requests
 */
public record TransferRequest(
    @NotNull(message = "Recipient user ID cannot be null")
    Long recipientUserId,

    @ValidCurrencyCode
    String senderCurrency,

    @ValidCurrencyCode
    String recipientCurrency,

    @ValidAmount
    BigDecimal amount
) {}
