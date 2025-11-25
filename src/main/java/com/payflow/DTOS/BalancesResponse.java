package com.payflow.DTOS;

import java.math.BigDecimal;
import java.util.Map;

public record BalancesResponse(
    Long walletId,
    Map<String, BigDecimal> balances
) {}
