package com.payflow.dto.v1.response;

import java.math.BigDecimal;
import java.util.Map;

public record WalletResponse(
    Long id,
    Map<String, BigDecimal> balances
) {}
