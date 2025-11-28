package com.payflow.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * CORS Configuration Properties
 * Maps to: app.security.cors in application.yml
 */
@Configuration
@ConfigurationProperties(prefix = "app.security.cors")
@Validated
public class CorsProperties {

  @NotEmpty(message = "CORS allowed origins must not be empty")
  private String[] allowedOrigins;

  @NotEmpty(message = "CORS allowed methods must not be empty")
  private String[] allowedMethods = new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS"};

  @NotEmpty(message = "CORS allowed headers must not be empty")
  private String[] allowedHeaders = new String[]{"*"};

  @NotNull(message = "CORS allow credentials must be specified")
  private Boolean allowCredentials = true;

  @NotNull(message = "CORS max age must be specified")
  private Long maxAge = 3600L;

  // Getters and Setters
  public String[] getAllowedOrigins() {
    return allowedOrigins;
  }

  public void setAllowedOrigins(String[] allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }

  public String[] getAllowedMethods() {
    return allowedMethods;
  }

  public void setAllowedMethods(String[] allowedMethods) {
    this.allowedMethods = allowedMethods;
  }

  public String[] getAllowedHeaders() {
    return allowedHeaders;
  }

  public void setAllowedHeaders(String[] allowedHeaders) {
    this.allowedHeaders = allowedHeaders;
  }

  public Boolean getAllowCredentials() {
    return allowCredentials;
  }

  public void setAllowCredentials(Boolean allowCredentials) {
    this.allowCredentials = allowCredentials;
  }

  public Long getMaxAge() {
    return maxAge;
  }

  public void setMaxAge(Long maxAge) {
    this.maxAge = maxAge;
  }
}
