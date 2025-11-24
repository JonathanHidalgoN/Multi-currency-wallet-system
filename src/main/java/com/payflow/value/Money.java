package com.payflow.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Money class representing an immutable amount of a specific currency.
 *
 * This class follows financial institution practices:
 * - Immutable: once created, cannot be changed
 * - Fixed scale: always uses 2 decimal places (standard for most currencies)
 * - Banker's rounding: HALF_EVEN rounding mode minimizes rounding bias
 * - Currency coupling: amount and currency are bound together, preventing
 * mixing
 */
public final class Money implements Comparable<Money> {
  private static final int SCALE = 2;
  private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

  private final BigDecimal amount;
  private final String currency;

  private Money(BigDecimal amount, String currency) {
    if (amount == null) {
      throw new IllegalArgumentException("Amount cannot be null");
    }
    if (currency == null || currency.trim().isEmpty()) {
      throw new IllegalArgumentException("Currency cannot be null or empty");
    }

    this.amount = amount.setScale(SCALE, ROUNDING_MODE);
    this.currency = currency.trim().toUpperCase();
  }

  public static Money of(BigDecimal amount, String currency) {
    return new Money(amount, currency);
  }

  public static Money of(String amount, String currency) {
    try {
      return new Money(new BigDecimal(amount), currency);
    } catch (NumberFormatException e) {
      throw new NumberFormatException("Invalid amount format: '" + amount + "'");
    }
  }

  public static Money ofCents(long amountInCents, String currency) {
    return new Money(BigDecimal.valueOf(amountInCents, SCALE), currency);
  }

  public static Money zero(String currency) {
    return new Money(BigDecimal.ZERO, currency);
  }

  public Money add(Money other) {
    validateSameCurrency(other);
    return new Money(amount.add(other.amount), currency);
  }

  public Money subtract(Money other) {
    validateSameCurrency(other);
    return new Money(amount.subtract(other.amount), currency);
  }

  public Money multiply(BigDecimal factor) {
    if (factor == null) {
      throw new IllegalArgumentException("Factor cannot be null");
    }
    return new Money(amount.multiply(factor), currency);
  }

  public Money multiply(long factor) {
    return new Money(amount.multiply(BigDecimal.valueOf(factor)), currency);
  }

  public Money divide(BigDecimal divisor) {
    if (divisor == null) {
      throw new IllegalArgumentException("Divisor cannot be null");
    }
    if (divisor.compareTo(BigDecimal.ZERO) == 0) {
      throw new IllegalArgumentException("Cannot divide by zero");
    }
    return new Money(amount.divide(divisor, SCALE, ROUNDING_MODE), currency);
  }

  public Money divide(long divisor) {
    if (divisor == 0) {
      throw new IllegalArgumentException("Cannot divide by zero");
    }
    return new Money(amount.divide(BigDecimal.valueOf(divisor), SCALE, ROUNDING_MODE), currency);
  }

  public Money negate() {
    return new Money(amount.negate(), currency);
  }

  public boolean isGreaterThan(Money other) {
    validateSameCurrency(other);
    return amount.compareTo(other.amount) > 0;
  }

  public boolean isLessThan(Money other) {
    validateSameCurrency(other);
    return amount.compareTo(other.amount) < 0;
  }

  public boolean isEqual(Money other) {
    validateSameCurrency(other);
    return amount.compareTo(other.amount) == 0;
  }

  public boolean isGreaterThanOrEqual(Money other) {
    validateSameCurrency(other);
    return amount.compareTo(other.amount) >= 0;
  }

  public boolean isLessThanOrEqual(Money other) {
    validateSameCurrency(other);
    return amount.compareTo(other.amount) <= 0;
  }

  public boolean isZero() {
    return amount.compareTo(BigDecimal.ZERO) == 0;
  }

  public boolean isPositive() {
    return amount.compareTo(BigDecimal.ZERO) > 0;
  }

  public boolean isNegative() {
    return amount.compareTo(BigDecimal.ZERO) < 0;
  }

  public Money abs() {
    return new Money(amount.abs(), currency);
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Money))
      return false;
    Money money = (Money) o;
    return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, currency);
  }

  @Override
  public int compareTo(Money other) {
    validateSameCurrency(other);
    return amount.compareTo(other.amount);
  }

  @Override
  public String toString() {
    return String.format("%s %s", currency, amount);
  }

  private void validateSameCurrency(Money other) {
    if (other == null) {
      throw new IllegalArgumentException("Money to compare cannot be null");
    }
    if (!currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          String.format("Cannot operate on different currencies: %s vs %s", currency, other.currency));
    }
  }
}
