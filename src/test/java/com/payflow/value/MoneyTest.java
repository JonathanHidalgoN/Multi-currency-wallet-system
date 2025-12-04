package com.payflow.value;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

  @Test
  void testCreateMoneyFromBigDecimal() {
    Money money = Money.of(new BigDecimal("100.50"), "USD");

    assertEquals(new BigDecimal("100.50"), money.getAmount());
    assertEquals("USD", money.getCurrency());
  }

  @Test
  void testCreateMoneyFromString() {
    Money money = Money.of("100.50", "EUR");

    assertEquals(new BigDecimal("100.50"), money.getAmount());
    assertEquals("EUR", money.getCurrency());
  }

  @Test
  void testCreateMoneyFromCents() {
    Money money = Money.ofCents(10050, "USD");

    assertEquals(new BigDecimal("100.50"), money.getAmount());
    assertEquals("USD", money.getCurrency());
  }

  @Test
  void testCreateZeroMoney() {
    Money money = Money.zero("GBP");

    assertEquals(new BigDecimal("0.00"), money.getAmount());
    assertEquals("GBP", money.getCurrency());
    assertTrue(money.isZero());
  }

  @Test
  void testThrowExceptionWhenAmountNull() {
    assertThrows(IllegalArgumentException.class, () -> Money.of((BigDecimal) null, "USD"));
  }

  @Test
  void testThrowExceptionWhenCurrencyNull() {
    assertThrows(IllegalArgumentException.class, () -> Money.of(new BigDecimal("100"), null));
  }

  @Test
  void testThrowExceptionWhenCurrencyEmpty() {
    assertThrows(IllegalArgumentException.class, () -> Money.of(new BigDecimal("100"), ""));
  }

  @Test
  void testThrowExceptionForInvalidStringAmount() {
    assertThrows(NumberFormatException.class, () -> Money.of("invalid", "USD"));
  }

  @Test
  void testNormalizeCurrencyToUppercase() {
    Money money = Money.of("100", "usd");

    assertEquals("USD", money.getCurrency());
  }

  @Test
  void testFixedScaleOfTwo() {
    Money money = Money.of(new BigDecimal("100.12345"), "USD");

    assertEquals(2, money.getAmount().scale());
    assertEquals(new BigDecimal("100.12"), money.getAmount());
  }

  @Test
  void testBankersRounding() {
    Money money1 = Money.of(new BigDecimal("100.125"), "USD");
    assertEquals(new BigDecimal("100.12"), money1.getAmount());

    Money money2 = Money.of(new BigDecimal("100.135"), "USD");
    assertEquals(new BigDecimal("100.14"), money2.getAmount());
  }

  @Test
  void testAddMoney() {
    Money money1 = Money.of("100.00", "USD");
    Money money2 = Money.of("50.50", "USD");

    Money result = money1.add(money2);

    assertEquals(new BigDecimal("150.50"), result.getAmount());
    assertEquals("USD", result.getCurrency());
  }

  @Test
  void testAddMoneyDifferentCurrencies() {
    Money money1 = Money.of("100.00", "USD");
    Money money2 = Money.of("50.00", "EUR");

    assertThrows(IllegalArgumentException.class, () -> money1.add(money2));
  }

  @Test
  void testSubtractMoney() {
    Money money1 = Money.of("100.00", "USD");
    Money money2 = Money.of("30.50", "USD");

    Money result = money1.subtract(money2);

    assertEquals(new BigDecimal("69.50"), result.getAmount());
    assertEquals("USD", result.getCurrency());
  }

  @Test
  void testSubtractMoneyResultingInNegative() {
    Money money1 = Money.of("50.00", "USD");
    Money money2 = Money.of("100.00", "USD");

    Money result = money1.subtract(money2);

    assertEquals(new BigDecimal("-50.00"), result.getAmount());
    assertTrue(result.isNegative());
  }

  @Test
  void testSubtractMoneyDifferentCurrencies() {
    Money money1 = Money.of("100.00", "USD");
    Money money2 = Money.of("50.00", "EUR");

    assertThrows(IllegalArgumentException.class, () -> money1.subtract(money2));
  }

  @Test
  void testMultiplyByBigDecimal() {
    Money money = Money.of("100.00", "USD");

    Money result = money.multiply(new BigDecimal("1.5"));

    assertEquals(new BigDecimal("150.00"), result.getAmount());
    assertEquals("USD", result.getCurrency());
  }

  @Test
  void testMultiplyByLong() {
    Money money = Money.of("100.00", "USD");

    Money result = money.multiply(3L);

    assertEquals(new BigDecimal("300.00"), result.getAmount());
    assertEquals("USD", result.getCurrency());
  }

  @Test
  void testThrowExceptionWhenMultiplyingByNull() {
    Money money = Money.of("100.00", "USD");

    assertThrows(IllegalArgumentException.class, () -> money.multiply((BigDecimal) null));
  }

  @Test
  void testDivideByBigDecimal() {
    Money money = Money.of("100.00", "USD");

    Money result = money.divide(new BigDecimal("4"));

    assertEquals(new BigDecimal("25.00"), result.getAmount());
    assertEquals("USD", result.getCurrency());
  }

  @Test
  void testDivideByLong() {
    Money money = Money.of("100.00", "USD");

    Money result = money.divide(5L);

    assertEquals(new BigDecimal("20.00"), result.getAmount());
    assertEquals("USD", result.getCurrency());
  }

  @Test
  void testDivideWithRounding() {
    Money money = Money.of("100.00", "USD");

    Money result = money.divide(3L);

    assertEquals(new BigDecimal("33.33"), result.getAmount());
  }

  @Test
  void testThrowExceptionWhenDividingByZero() {
    Money money = Money.of("100.00", "USD");

    assertThrows(IllegalArgumentException.class, () -> money.divide(BigDecimal.ZERO));
  }

  @Test
  void testThrowExceptionWhenDividingByZeroLong() {
    Money money = Money.of("100.00", "USD");

    assertThrows(IllegalArgumentException.class, () -> money.divide(0));
  }

  @Test
  void testThrowExceptionWhenDividingByNull() {
    Money money = Money.of("100.00", "USD");

    assertThrows(IllegalArgumentException.class, () -> money.divide((BigDecimal) null));
  }

  @Test
  void testIsGreaterThan() {
    Money money1 = Money.of("100.00", "USD");
    Money money2 = Money.of("50.00", "USD");

    assertTrue(money1.isGreaterThan(money2));
    assertFalse(money2.isGreaterThan(money1));
  }

  @Test
  void testThrowExceptionWhenComparingDifferentCurrencies() {
    Money money1 = Money.of("100.00", "USD");
    Money money2 = Money.of("50.00", "EUR");

    assertThrows(IllegalArgumentException.class, () -> money1.isGreaterThan(money2));
  }

  @Test
  void testIsLessThan() {
    Money money1 = Money.of("50.00", "USD");
    Money money2 = Money.of("100.00", "USD");

    assertTrue(money1.isLessThan(money2));
    assertFalse(money2.isLessThan(money1));
  }

  @Test
  void testIsEqual() {
    Money money1 = Money.of("100.00", "USD");
    Money money2 = Money.of("100.00", "USD");
    Money money3 = Money.of("50.00", "USD");

    assertTrue(money1.isEqual(money2));
    assertFalse(money1.isEqual(money3));
  }

  @Test
  void testIsGreaterThanOrEqual() {
    Money money1 = Money.of("100.00", "USD");
    Money money2 = Money.of("100.00", "USD");
    Money money3 = Money.of("50.00", "USD");

    assertTrue(money1.isGreaterThanOrEqual(money2));
    assertTrue(money1.isGreaterThanOrEqual(money3));
    assertFalse(money3.isGreaterThanOrEqual(money1));
  }

  @Test
  void testIsLessThanOrEqual() {
    Money money1 = Money.of("100.00", "USD");
    Money money2 = Money.of("100.00", "USD");
    Money money3 = Money.of("150.00", "USD");

    assertTrue(money1.isLessThanOrEqual(money2));
    assertTrue(money1.isLessThanOrEqual(money3));
    assertFalse(money3.isLessThanOrEqual(money1));
  }

  @Test
  void testIsZero() {
    Money zero = Money.zero("USD");
    Money nonZero = Money.of("0.01", "USD");

    assertTrue(zero.isZero());
    assertFalse(nonZero.isZero());
  }

  @Test
  void testIsPositive() {
    Money positive = Money.of("100.00", "USD");
    Money zero = Money.zero("USD");
    Money negative = Money.of("-50.00", "USD");

    assertTrue(positive.isPositive());
    assertFalse(zero.isPositive());
    assertFalse(negative.isPositive());
  }

  @Test
  void testIsNegative() {
    Money negative = Money.of("-100.00", "USD");
    Money zero = Money.zero("USD");
    Money positive = Money.of("50.00", "USD");

    assertTrue(negative.isNegative());
    assertFalse(zero.isNegative());
    assertFalse(positive.isNegative());
  }

  @Test
  void testAbs() {
    Money positive = Money.of("100.00", "USD");
    Money negative = Money.of("-100.00", "USD");

    assertEquals(new BigDecimal("100.00"), positive.abs().getAmount());
    assertEquals(new BigDecimal("100.00"), negative.abs().getAmount());
    assertEquals("USD", negative.abs().getCurrency());
  }

  @Test
  void testNegate() {
    Money positive = Money.of("100.00", "USD");
    Money negative = Money.of("-50.00", "USD");

    assertEquals(new BigDecimal("-100.00"), positive.negate().getAmount());
    assertEquals(new BigDecimal("50.00"), negative.negate().getAmount());
    assertEquals("USD", negative.negate().getCurrency());
  }

  @Test
  void testCompareTo() {
    Money money1 = Money.of("100.00", "USD");
    Money money2 = Money.of("100.00", "USD");
    Money money3 = Money.of("50.00", "USD");

    assertEquals(0, money1.compareTo(money2));
    assertTrue(money1.compareTo(money3) > 0);
    assertTrue(money3.compareTo(money1) < 0);
  }

  @Test
  void testToString() {
    Money money = Money.of("100.50", "USD");

    assertEquals("USD 100.50", money.toString());
  }

  @Test
  void testEquals() {
    Money money1 = Money.of("100.00", "USD");
    Money money2 = Money.of("100.00", "USD");
    Money money3 = Money.of("100.00", "EUR");
    Money money4 = Money.of("50.00", "USD");

    assertEquals(money1, money2);
    assertNotEquals(money1, money3);
    assertNotEquals(money1, money4);
    assertNotEquals(money1, null);
    assertNotEquals(money1, "USD 100.00");
  }

  @Test
  void testHashCode() {
    Money money1 = Money.of("100.00", "USD");
    Money money2 = Money.of("100.00", "USD");

    assertEquals(money1.hashCode(), money2.hashCode());
  }

  @Test
  void testImmutability() {
    Money original = Money.of("100.00", "USD");
    Money result = original.add(Money.of("50.00", "USD"));

    assertEquals(new BigDecimal("100.00"), original.getAmount());
    assertEquals(new BigDecimal("150.00"), result.getAmount());
  }

  @ParameterizedTest
  @ValueSource(strings = { "0.01", "100.00", "999999.99", "0.00" })
  void testVariousValidAmounts(String amount) {
    Money money = Money.of(amount, "USD");

    assertNotNull(money);
    assertEquals(2, money.getAmount().scale());
  }

  @Test
  void testTrimCurrency() {
    Money money = Money.of("100.00", "  USD  ");

    assertEquals("USD", money.getCurrency());
  }

  @Test
  void testThrowExceptionWhenComparingWithNull() {
    Money money = Money.of("100.00", "USD");

    assertThrows(IllegalArgumentException.class, () -> money.isGreaterThan(null));
  }

  @Test
  void testComplexArithmeticChain() {
    Money money = Money.of("100.00", "USD");

    Money result = money
        .add(Money.of("50.00", "USD"))
        .subtract(Money.of("20.00", "USD"))
        .multiply(2L)
        .divide(3L);

    assertEquals(new BigDecimal("86.67"), result.getAmount());
    assertEquals("USD", result.getCurrency());
  }
}
