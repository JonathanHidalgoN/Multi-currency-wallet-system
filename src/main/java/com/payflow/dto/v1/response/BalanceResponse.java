package com.payflow.dto.v1.response;

import java.math.BigDecimal;

public record BalanceResponse(
    String currency,
    BigDecimal balance
) {}
