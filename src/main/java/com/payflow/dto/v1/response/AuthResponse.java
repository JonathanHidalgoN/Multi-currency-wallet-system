package com.payflow.dto.v1.response;

public record AuthResponse(
    Long id,
    String email,
    String fullName,
    String token,
    String message
) {}
