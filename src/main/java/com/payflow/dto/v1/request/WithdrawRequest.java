package com.payflow.dto.v1.request;

import com.payflow.validation.ValidationAnnotations.ValidAmount;
import com.payflow.validation.ValidationAnnotations.ValidCurrencyCode;

import java.math.BigDecimal;

/**
 * DTO for withdrawal requests
 */
public record WithdrawRequest(
    @ValidAmount
    BigDecimal amount,

    @ValidCurrencyCode
    String currency
) {}
