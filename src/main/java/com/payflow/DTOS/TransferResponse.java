package com.payflow.DTOS;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
    String transactionId,
    String type,
    String senderCurrency,
    BigDecimal senderAmount,
    BigDecimal fee,
    BigDecimal totalDebit,
    String recipientCurrency,
    BigDecimal recipientAmount,
    BigDecimal exchangeRate,
    String status,
    LocalDateTime createdAt
) {}
