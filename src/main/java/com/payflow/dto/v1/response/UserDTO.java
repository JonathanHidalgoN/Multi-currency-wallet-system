package com.payflow.dto.v1.response;

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
