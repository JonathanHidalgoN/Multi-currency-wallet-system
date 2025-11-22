package com.payflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
  private Jwt jwt = new Jwt();
  private Security security = new Security();
  private ExchangeRate exchangeRate = new ExchangeRate();

  public AppProperties() {
  }

  public Jwt getJwt() {
    return jwt;
  }

  public void setJwt(Jwt jwt) {
    this.jwt = jwt;
  }

  public Security getSecurity() {
    return security;
  }

  public void setSecurity(Security security) {
    this.security = security;
  }

  public ExchangeRate getExchangeRate() {
    return exchangeRate;
  }

  public void setExchangeRate(ExchangeRate exchangeRate) {
    this.exchangeRate = exchangeRate;
  }

  /**
   * JWT Configuration
   */
  public static class Jwt {
    private String secret;
    private Long expiration;

    public Jwt() {
    }

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

  /**
   * Security Configuration (CORS, etc.)
   */
  public static class Security {
    private Cors cors = new Cors();

    public Security() {
    }

    public Cors getCors() {
      return cors;
    }

    public void setCors(Cors cors) {
      this.cors = cors;
    }

    public static class Cors {
      private String[] allowedOrigins;
      private String[] allowedMethods;
      private String[] allowedHeaders;
      private Boolean allowCredentials;
      private Long maxAge;

      public Cors() {
      }

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
  }

  /**
   * Exchange Rate API Configuration
   */
  public static class ExchangeRate {
    private String apiUrl;
    private String apiKey;
    private Integer cacheDurationMinutes;

    public ExchangeRate() {
    }

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
}
