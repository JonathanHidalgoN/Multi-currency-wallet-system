package com.payflow.dto.v1.request;

import com.payflow.validation.ValidationAnnotations.ValidEmail;
import com.payflow.validation.ValidationAnnotations.ValidPassword;
import com.payflow.validation.ValidationAnnotations.ValidFullName;

public record RegisterRequest(
    @ValidEmail String email,

    @ValidPassword String password,

    @ValidFullName String fullName) {
}
