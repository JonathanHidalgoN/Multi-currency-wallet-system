package com.payflow.DTOS;

public record AuthResponse(
    Long id,
    String email,
    String fullName,
    String token,
    String message
) {}
