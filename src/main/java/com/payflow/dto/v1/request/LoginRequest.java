package com.payflow.dto.v1.request;

import com.payflow.validation.ValidationAnnotations.ValidEmail;
import com.payflow.validation.ValidationAnnotations.ValidPassword;

public record LoginRequest(
    @ValidEmail
    String email,

    @ValidPassword
    String password
) {}
