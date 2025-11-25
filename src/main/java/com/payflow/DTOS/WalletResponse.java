package com.payflow.DTOS;

import java.math.BigDecimal;
import java.util.Map;

public record WalletResponse(
    Long id,
    Map<String, BigDecimal> balances
) {}
