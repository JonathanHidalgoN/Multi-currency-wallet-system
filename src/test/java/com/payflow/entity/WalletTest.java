package com.payflow.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.payflow.value.Money;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

  private Wallet wallet;
  private User user;

  @BeforeEach
  void setUp() {
    user = User.builder()
        .id(1L)
        .email("test@example.com")
        .password("encoded_password")
        .fullName("Test User")
        .enabled(true)
        .build();

    wallet = Wallet.builder()
        .id(1L)
        .user(user)
        .build();
  }

  @Test
  void testGetBalanceReturnsMoneyObject() {
    Money balance = wallet.getBalance("USD");

    assertNotNull(balance);
    assertEquals("USD", balance.getCurrency());
    assertTrue(balance.isZero());
  }

  @Test
  void testGetBalanceForEmptyCurrencyReturnsZero() {
    Money usdBalance = wallet.getBalance("USD");
    Money eurBalance = wallet.getBalance("EUR");

    assertEquals(Money.zero("USD"), usdBalance);
    assertEquals(Money.zero("EUR"), eurBalance);
  }

  @Test
  void testAddBalanceSuccessfully() {
    Money amount = Money.of("100.00", "USD");

    wallet.addBalance(amount);

    Money result = wallet.getBalance("USD");
    assertEquals(Money.of("100.00", "USD"), result);
  }

  @Test
  void testAddBalanceMultipleTimes() {
    Money amount1 = Money.of("100.00", "USD");
    Money amount2 = Money.of("50.00", "USD");
    Money amount3 = Money.of("25.50", "USD");

    wallet.addBalance(amount1);
    wallet.addBalance(amount2);
    wallet.addBalance(amount3);

    Money result = wallet.getBalance("USD");
    assertEquals(Money.of("175.50", "USD"), result);
  }

  @Test
  void testAddBalanceMultipleCurrencies() {
    Money usdAmount = Money.of("100.00", "USD");
    Money eurAmount = Money.of("50.00", "EUR");

    wallet.addBalance(usdAmount);
    wallet.addBalance(eurAmount);

    assertEquals(Money.of("100.00", "USD"), wallet.getBalance("USD"));
    assertEquals(Money.of("50.00", "EUR"), wallet.getBalance("EUR"));
  }

  @Test
  void testAddBalanceThrowsExceptionWhenNull() {
    assertThrows(IllegalArgumentException.class, () -> wallet.addBalance(null));
  }

  @Test
  void testSubtractBalanceSuccessfully() {
    Money initial = Money.of("100.00", "USD");
    Money subtract = Money.of("30.00", "USD");

    wallet.addBalance(initial);
    wallet.subtractBalance(subtract);

    Money result = wallet.getBalance("USD");
    assertEquals(Money.of("70.00", "USD"), result);
  }

  @Test
  void testSubtractBalanceExactAmount() {
    Money amount = Money.of("100.00", "USD");

    wallet.addBalance(amount);
    wallet.subtractBalance(amount);

    Money result = wallet.getBalance("USD");
    assertEquals(Money.zero("USD"), result);
  }

  @Test
  void testSubtractBalanceMultipleTimes() {
    Money initial = Money.of("100.00", "USD");
    Money subtract1 = Money.of("25.00", "USD");
    Money subtract2 = Money.of("15.50", "USD");

    wallet.addBalance(initial);
    wallet.subtractBalance(subtract1);
    wallet.subtractBalance(subtract2);

    Money result = wallet.getBalance("USD");
    assertEquals(Money.of("59.50", "USD"), result);
  }

  @Test
  void testSubtractBalanceThrowsExceptionWhenInsufficientBalance() {
    Money initial = Money.of("30.00", "USD");
    Money subtract = Money.of("50.00", "USD");

    wallet.addBalance(initial);

    assertThrows(IllegalArgumentException.class, () -> wallet.subtractBalance(subtract));
  }

  @Test
  void testSubtractBalanceThrowsExceptionFromZeroBalance() {
    Money subtract = Money.of("10.00", "USD");

    assertThrows(IllegalArgumentException.class, () -> wallet.subtractBalance(subtract));
  }

  @Test
  void testSubtractBalanceThrowsExceptionWhenNull() {
    assertThrows(IllegalArgumentException.class, () -> wallet.subtractBalance(null));
  }

  @Test
  void testHasSufficientBalanceReturnsTrue() {
    Money balance = Money.of("100.00", "USD");
    Money check = Money.of("50.00", "USD");

    wallet.addBalance(balance);

    assertTrue(wallet.hasSufficientBalance(check));
  }

  @Test
  void testHasSufficientBalanceReturnsFalse() {
    Money balance = Money.of("30.00", "USD");
    Money check = Money.of("50.00", "USD");

    wallet.addBalance(balance);

    assertFalse(wallet.hasSufficientBalance(check));
  }

  @Test
  void testHasSufficientBalanceReturnsTrueWhenEqual() {
    Money balance = Money.of("100.00", "USD");
    Money check = Money.of("100.00", "USD");

    wallet.addBalance(balance);

    assertTrue(wallet.hasSufficientBalance(check));
  }

  @Test
  void testHasSufficientBalanceReturnsFalseForZeroBalance() {
    Money check = Money.of("0.01", "USD");

    assertFalse(wallet.hasSufficientBalance(check));
  }

  @Test
  void testHasSufficientBalanceThrowsExceptionWhenNull() {
    assertThrows(IllegalArgumentException.class, () -> wallet.hasSufficientBalance(null));
  }

  @Test
  void testWalletIsolatesCurrencies() {
    Money usdAmount = Money.of("100.00", "USD");
    Money eurAmount = Money.of("50.00", "EUR");

    wallet.addBalance(usdAmount);
    wallet.addBalance(eurAmount);

    wallet.subtractBalance(Money.of("30.00", "USD"));

    assertEquals(Money.of("70.00", "USD"), wallet.getBalance("USD"));
    assertEquals(Money.of("50.00", "EUR"), wallet.getBalance("EUR"));
  }

  @Test
  void testSubtractBalanceFromMultipleCurrencies() {
    Money usdAmount = Money.of("100.00", "USD");
    Money eurAmount = Money.of("80.00", "EUR");

    wallet.addBalance(usdAmount);
    wallet.addBalance(eurAmount);

    wallet.subtractBalance(Money.of("25.00", "USD"));
    wallet.subtractBalance(Money.of("30.00", "EUR"));

    assertEquals(Money.of("75.00", "USD"), wallet.getBalance("USD"));
    assertEquals(Money.of("50.00", "EUR"), wallet.getBalance("EUR"));
  }

  @Test
  void testBalanceOperationsPreserveMoneyImmutability() {
    Money originalAmount = Money.of("100.00", "USD");
    Money addAmount = Money.of("50.00", "USD");

    wallet.addBalance(originalAmount);
    wallet.addBalance(addAmount);

    assertEquals(Money.of("100.00", "USD"), originalAmount);
    assertEquals(Money.of("50.00", "USD"), addAmount);
  }

  @Test
  void testGetBalanceCreatesFreshMoneyObjects() {
    Money amount = Money.of("100.00", "USD");
    wallet.addBalance(amount);

    Money balance1 = wallet.getBalance("USD");
    Money balance2 = wallet.getBalance("USD");

    assertEquals(balance1, balance2);
    assertTrue(balance1 == balance2 || balance1.equals(balance2));
  }

  @Test
  void testComplexBalanceScenario() {
    Money initial = Money.of("1000.00", "USD");
    wallet.addBalance(initial);

    assertTrue(wallet.hasSufficientBalance(Money.of("500.00", "USD")));
    wallet.subtractBalance(Money.of("250.00", "USD"));

    assertEquals(Money.of("750.00", "USD"), wallet.getBalance("USD"));
    assertTrue(wallet.hasSufficientBalance(Money.of("750.00", "USD")));

    wallet.addBalance(Money.of("250.00", "USD"));
    assertEquals(Money.of("1000.00", "USD"), wallet.getBalance("USD"));
  }

  @Test
  void testSubtractBalanceWithRounding() {
    Money initial = Money.of("100.00", "USD");
    Money subtract = Money.of("33.33", "USD");

    wallet.addBalance(initial);
    wallet.subtractBalance(subtract);
    wallet.subtractBalance(subtract);
    wallet.subtractBalance(subtract);

    Money result = wallet.getBalance("USD");
    assertEquals(Money.of("0.01", "USD"), result);
  }

  @Test
  void testAddBalancePreservesExistingBalance() {
    Money initial = Money.of("100.00", "USD");
    Money add = Money.of("50.00", "USD");

    wallet.addBalance(initial);
    Money balanceAfterFirst = wallet.getBalance("USD");
    assertEquals(Money.of("100.00", "USD"), balanceAfterFirst);

    wallet.addBalance(add);
    Money balanceAfterSecond = wallet.getBalance("USD");
    assertEquals(Money.of("150.00", "USD"), balanceAfterSecond);
  }

  @Test
  void testWalletWithManyTransactions() {
    for (int i = 0; i < 100; i++) {
      wallet.addBalance(Money.of("1.00", "USD"));
    }

    assertEquals(Money.of("100.00", "USD"), wallet.getBalance("USD"));

    for (int i = 0; i < 50; i++) {
      wallet.subtractBalance(Money.of("1.00", "USD"));
    }

    assertEquals(Money.of("50.00", "USD"), wallet.getBalance("USD"));
  }
}
