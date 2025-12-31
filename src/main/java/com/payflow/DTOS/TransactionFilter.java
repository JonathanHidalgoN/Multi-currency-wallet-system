package com.payflow.DTOS;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.payflow.entity.Transaction;

public record TransactionFilter(
    String currency,
    Transaction.TransactionType type,
    Transaction.TransactionStatus status,
    LocalDate fromDate,
    LocalDate toDate,
    BigDecimal minAmount,
    BigDecimal maxAmount) {
}
