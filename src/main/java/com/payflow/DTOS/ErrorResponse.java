package com.payflow.DTOS;

import java.util.Map;

public record ErrorResponse(
    int status,
    String message,
    Map<String, String> errors
) {}
