package com.payflow.dto.v1.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDTO(
    String transactionId,
    String type,
    BigDecimal amount,
    String currency,
    String status,
    LocalDateTime createdAt) {
}
