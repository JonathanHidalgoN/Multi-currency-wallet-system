package com.payflow.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeRateServiceTest {

  private ExchangeRateService exchangeRateService;

  @BeforeEach
  void setUp() {
    exchangeRateService = new ExchangeRateService();
  }

  @Test
  void shouldReturnOneForSameCurrency() {
    BigDecimal rate = exchangeRateService.getExchangeRate("USD", "USD");
    assertEquals(BigDecimal.ONE, rate);

    rate = exchangeRateService.getExchangeRate("EUR", "EUR");
    assertEquals(BigDecimal.ONE, rate);
  }

  @Test
  void shouldReturnPositiveRateForValidPairs() {
    BigDecimal rate = exchangeRateService.getExchangeRate("USD", "EUR");

    assertNotNull(rate);
    assertTrue(rate.compareTo(BigDecimal.ZERO) > 0);
  }

  @Test
  void shouldReturnTrueWhenRateExists() {
    assertTrue(exchangeRateService.hasExchangeRate("USD", "EUR"));
    assertTrue(exchangeRateService.hasExchangeRate("EUR", "USD"));
  }

  @Test
  void shouldReturnTrueForSameCurrency() {
    assertTrue(exchangeRateService.hasExchangeRate("USD", "USD"));
    assertTrue(exchangeRateService.hasExchangeRate("EUR", "EUR"));
  }

  @Test
  @DisplayName("Should return false when exchange rate does not exist")
  void shouldReturnFalseWhenRateDoesNotExist() {
    assertFalse(exchangeRateService.hasExchangeRate("NOEXIST", "JPY"));
    assertFalse(exchangeRateService.hasExchangeRate("NOEXIST1", "USD"));
  }

  @Test
  void shouldBeConsistentBetweenHasAndGet() {
    if (exchangeRateService.hasExchangeRate("USD", "EUR")) {
      assertDoesNotThrow(() -> exchangeRateService.getExchangeRate("USD", "EUR"));
    }

    if (!exchangeRateService.hasExchangeRate("NOEXIST", "JPY")) {
      assertThrows(IllegalArgumentException.class,
          () -> exchangeRateService.getExchangeRate("USD", "JPY"));
    }
  }

  @Test
  void shouldHandleMultipleConversionsWithoutStateIssues() {
    BigDecimal rate1 = exchangeRateService.getExchangeRate("USD", "EUR");
    BigDecimal rate2 = exchangeRateService.getExchangeRate("USD", "EUR");

    assertEquals(rate1, rate2, "Same exchange rate should be returned for repeated calls");
  }
}
