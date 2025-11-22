package com.payflow.DTOS;

public record UserResponse(
    Long id,
    String email,
    String fullName
) {}
