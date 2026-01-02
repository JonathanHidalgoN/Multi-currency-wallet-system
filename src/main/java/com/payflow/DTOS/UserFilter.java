package com.payflow.DTOS;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import jakarta.validation.constraints.PastOrPresent;

public record UserFilter(
    String email,
    String fullName,
    Boolean enabled,
    @DateTimeFormat(iso = ISO.DATE)
    @PastOrPresent(message = "From date cannot be in the future")
    LocalDate fromDate,
    @DateTimeFormat(iso = ISO.DATE)
    @PastOrPresent(message = "To date cannot be in the future")
    LocalDate toDate,
    String roleName) {
}
