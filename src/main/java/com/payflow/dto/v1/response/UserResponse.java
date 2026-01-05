package com.payflow.dto.v1.response;

public record UserResponse(
    Long id,
    String email,
    String fullName
) {}
