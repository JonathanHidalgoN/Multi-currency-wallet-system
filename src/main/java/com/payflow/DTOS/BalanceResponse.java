package com.payflow.DTOS;

import java.math.BigDecimal;

public record BalanceResponse(
    String currency,
    BigDecimal balance
) {}
