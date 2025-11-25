package com.payflow.services;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExchangeRateService implements IExchangeRateService {

  private final Map<String, BigDecimal> exchangeRates;

  public ExchangeRateService() {
    this.exchangeRates = initializeExchangeRates();
  }

  private Map<String, BigDecimal> initializeExchangeRates() {
    Map<String, BigDecimal> rates = new HashMap<>();

    rates.put("USD-EUR", new BigDecimal("0.92"));
    rates.put("USD-MXN", new BigDecimal("17.50"));

    rates.put("EUR-USD", new BigDecimal("1.09"));
    rates.put("EUR-MXN", new BigDecimal("19.02"));

    rates.put("MXN-USD", new BigDecimal("0.057"));
    rates.put("MXN-EUR", new BigDecimal("0.053"));

    return rates;
  }

  @Override
  public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
    if (fromCurrency.equals(toCurrency)) {
      return BigDecimal.ONE;
    }

    String key = fromCurrency + "-" + toCurrency;
    BigDecimal rate = exchangeRates.get(key);

    if (rate == null) {
      throw new IllegalArgumentException(
          String.format("Exchange rate not available for %s to %s", fromCurrency, toCurrency));
    }

    return rate;
  }

  @Override
  public boolean hasExchangeRate(String fromCurrency, String toCurrency) {
    if (fromCurrency.equals(toCurrency)) {
      return true;
    }

    String key = fromCurrency + "-" + toCurrency;
    return exchangeRates.containsKey(key);
  }
}
