package com.payflow.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * JWT Configuration Properties
 * Maps to: app.jwt in application.yml
 */
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Validated
public class JwtProperties {

  @NotBlank(message = "JWT secret must not be blank")
  @Size(min = 32, message = "JWT secret must be at least 32 characters for HS256")
  private String secret;

  @Min(value = 60000, message = "JWT expiration must be at least 60 seconds (60000ms)")
  private Long expiration = 86400000L; // 24 hours default

  // Getters and Setters
  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public Long getExpiration() {
    return expiration;
  }

  public void setExpiration(Long expiration) {
    this.expiration = expiration;
  }
}
