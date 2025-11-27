package com.payflow.exception;

public class RateLimitExceededException extends RuntimeException {

  public RateLimitExceededException(String message) {
    super(message);
  }

  public RateLimitExceededException(String identifier, String rateLimitType) {
    super(String.format("Rate limit exceeded for %s: %s", rateLimitType, identifier));
  }
}
