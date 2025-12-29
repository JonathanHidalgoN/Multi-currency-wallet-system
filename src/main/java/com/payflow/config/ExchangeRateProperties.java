package com.payflow.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Exchange Rate API Configuration Properties
 * Maps to: app.exchange-rate in application.yml
 */
@Configuration
@ConfigurationProperties(prefix = "app.exchange-rate")
@Validated
public class ExchangeRateProperties {

  @NotBlank(message = "Exchange rate API URL must not be blank")
  private String apiUrl;

  private String apiKey;

  @Min(value = 1, message = "Cache duration must be at least 1 minute")
  private Integer cacheDurationMinutes = 5;

  public String getApiUrl() {
    return apiUrl;
  }

  public void setApiUrl(String apiUrl) {
    this.apiUrl = apiUrl;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public Integer getCacheDurationMinutes() {
    return cacheDurationMinutes;
  }

  public void setCacheDurationMinutes(Integer cacheDurationMinutes) {
    this.cacheDurationMinutes = cacheDurationMinutes;
  }
}
