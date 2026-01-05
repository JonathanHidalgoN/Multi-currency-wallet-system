package com.payflow.dto.v1.response;

import java.util.Map;

public record ErrorResponse(
    int status,
    String message,
    Map<String, String> errors
) {}
