package com.payflow.DTOS;

import java.time.LocalDateTime;
import java.util.Set;

public record UserDTO(
    Long id,
    String email,
    String fullName,
    Boolean enabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Set<String> roles) {

}
