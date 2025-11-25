package com.payflow.services;

import java.math.BigDecimal;

/**
 * Service for handling currency exchange rates
 */
public interface IExchangeRateService {

  /**
   * Get the exchange rate between two currencies
   *
   * @param fromCurrency the source currency code (e.g., "USD")
   * @param toCurrency   the target currency code (e.g., "EUR")
   * @return the exchange rate as a BigDecimal
   * @throws IllegalArgumentException if exchange rate is not available
   */
  BigDecimal getExchangeRate(String fromCurrency, String toCurrency);

  /**
   * Check if an exchange rate exists between two currencies
   *
   * @param fromCurrency the source currency code
   * @param toCurrency   the target currency code
   * @return true if exchange rate exists, false otherwise
   */
  boolean hasExchangeRate(String fromCurrency, String toCurrency);
}
