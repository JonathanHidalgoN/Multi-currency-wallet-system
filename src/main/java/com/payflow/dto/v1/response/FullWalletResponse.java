package com.payflow.dto.v1.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record FullWalletResponse(
    Long id,
    Long userId,
    Map<String, BigDecimal> balances,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
