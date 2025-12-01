package com.payflow.exception;

public class DuplicateEmailException extends RuntimeException {

  private final String email;

  public DuplicateEmailException(String email) {
    super("Email already exists: " + email);
    this.email = email;
  }

  public String getEmail() {
    return email;
  }
}
