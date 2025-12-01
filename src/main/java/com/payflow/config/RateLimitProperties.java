package com.payflow.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Rate Limit Configuration Properties
 * Maps to: app.rate-limit in application.yml
 */
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
@Validated
public class RateLimitProperties {

  @NotNull(message = "Rate limit enabled flag must be specified")
  private Boolean enabled = true;

  @NotNull(message = "IP capacity must be specified")
  private Integer ipCapacity = 5;

  @NotNull(message = "IP refill rate must be specified")
  private Integer ipRefillRate = 5;

  @NotNull(message = "IP refill duration in minutes must be specified")
  private Integer ipRefillDurationMinutes = 1;

  @NotNull(message = "User capacity must be specified")
  private Integer userCapacity = 100;

  @NotNull(message = "User refill rate must be specified")
  private Integer userRefillRate = 100;

  @NotNull(message = "User refill duration in minutes must be specified")
  private Integer userRefillDurationMinutes = 1;

  // Getters and Setters
  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public Integer getIpCapacity() {
    return ipCapacity;
  }

  public void setIpCapacity(Integer ipCapacity) {
    this.ipCapacity = ipCapacity;
  }

  public Integer getIpRefillRate() {
    return ipRefillRate;
  }

  public void setIpRefillRate(Integer ipRefillRate) {
    this.ipRefillRate = ipRefillRate;
  }

  public Integer getIpRefillDurationMinutes() {
    return ipRefillDurationMinutes;
  }

  public void setIpRefillDurationMinutes(Integer ipRefillDurationMinutes) {
    this.ipRefillDurationMinutes = ipRefillDurationMinutes;
  }

  public Integer getUserCapacity() {
    return userCapacity;
  }

  public void setUserCapacity(Integer userCapacity) {
    this.userCapacity = userCapacity;
  }

  public Integer getUserRefillRate() {
    return userRefillRate;
  }

  public void setUserRefillRate(Integer userRefillRate) {
    this.userRefillRate = userRefillRate;
  }

  public Integer getUserRefillDurationMinutes() {
    return userRefillDurationMinutes;
  }

  public void setUserRefillDurationMinutes(Integer userRefillDurationMinutes) {
    this.userRefillDurationMinutes = userRefillDurationMinutes;
  }
}
