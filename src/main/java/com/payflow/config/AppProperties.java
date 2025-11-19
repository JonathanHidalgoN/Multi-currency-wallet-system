package com.payflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
  private Jwt jwt = new Jwt();
  private Security security = new Security();
  private ExchangeRate exchangeRate = new ExchangeRate();

  @Data
  public static class Jwt {
    private String secret;
    private Long expiration;
  }

  @Data
  public static class Security {
    private Cors cors = new Cors();

    @Data
    public static class Cors {
      private String[] allowedOrigins;
      private String[] allowedMethods;
      private String[] allowedHeaders;
      private Boolean allowCredentials;
      private Long maxAge;
    }
  }

  @Data
  public static class ExchangeRate {
    private String apiUrl;
    private String apiKey;
    private Integer cacheDurationMinutes;
  }

}
