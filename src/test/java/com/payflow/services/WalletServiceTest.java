package com.payflow.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.payflow.entity.User;
import com.payflow.entity.Wallet;
import com.payflow.repository.IWalletRepository;
import com.payflow.value.Money;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("Wallet service tests")
class WalletServiceTest {

  @Mock
  private IWalletRepository walletRepository;

  @InjectMocks
  private WalletService walletService;

  private User user;
  private Wallet wallet;

  @BeforeEach
  void setUp() {
    user = User.builder()
        .id(1L)
        .email("test@example.com")
        .password("encoded_password")
        .fullName("Test user")
        .enabled(true)
        .build();

    wallet = Wallet.builder()
        .id(1L)
        .user(user)
        .build();
  }

  @Test
  void shouldCreateWalletForUserSuccessfully() {
    when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

    Wallet result = walletService.createWalletForUser(user);

    assertNotNull(result);
    assertEquals(wallet.getId(), result.getId());
    assertEquals(user.getId(), result.getUser().getId());
    verify(walletRepository).save(any(Wallet.class));
  }

  @Test
  void shouldReturnWalletWhenFoundByUser() {
    when(walletRepository.findByUserWithoutLock(user)).thenReturn(Optional.of(wallet));

    Wallet result = walletService.getWalletByUserReadOnly(user);

    assertNotNull(result);
    assertEquals(wallet.getId(), result.getId());
    verify(walletRepository).findByUserWithoutLock(user);
  }

  @Test
  void shouldThrowExceptionWhenWalletNotFoundByUser() {
    when(walletRepository.findByUserWithoutLock(user)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> walletService.getWalletByUserReadOnly(user));

    verify(walletRepository).findByUserWithoutLock(user);
  }

  @Test
  void shouldReturnWalletWhenFoundByUserId() {
    Long userId = 1L;
    when(walletRepository.findByUserIdWithoutLock(userId)).thenReturn(Optional.of(wallet));

    Wallet result = walletService.getWalletByUserIdReadOnly(userId);

    assertNotNull(result);
    assertEquals(wallet.getId(), result.getId());
    verify(walletRepository).findByUserIdWithoutLock(userId);
  }

  @Test
  void shouldThrowExceptionWhenWalletNotFoundByUserId() {
    Long userId = 999L;
    when(walletRepository.findByUserIdWithoutLock(userId)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> walletService.getWalletByUserIdReadOnly(userId));

    verify(walletRepository).findByUserIdWithoutLock(userId);
  }

  @Test
  void shouldReturnBalanceForGivenCurrency() {
    String currency = "USD";
    Money expectedBalance = Money.of("100.00", currency);
    wallet.addBalance(expectedBalance);

    Money result = walletService.getBalance(wallet, currency);

    assertEquals(expectedBalance, result);
  }

  @Test
  void shouldReturnTrueWhenSufficientBalance() {
    String currency = "USD";
    Money balance = Money.of("100.00", currency);
    Money requestedAmount = Money.of("50.00", currency);
    wallet.addBalance(balance);

    boolean result = walletService.hasSufficientBalance(wallet, requestedAmount);

    assertTrue(result);
  }

  @Test
  void shouldReturnFalseWhenInsufficientBalance() {
    String currency = "USD";
    Money balance = Money.of("30.00", currency);
    Money requestedAmount = Money.of("50.00", currency);
    wallet.addBalance(balance);

    boolean result = walletService.hasSufficientBalance(wallet, requestedAmount);

    assertFalse(result);
  }

  @Test
  void shouldReturnTrueWhenBalanceEqualsRequestedAmount() {
    String currency = "USD";
    Money balance = Money.of("50.00", currency);
    wallet.addBalance(balance);

    boolean result = walletService.hasSufficientBalance(wallet, balance);

    assertTrue(result);
  }

  @Test
  void shouldAddBalanceSuccessfully() {
    String currency = "USD";
    Money initialAmount = Money.of("100.00", currency);
    Money addAmount = Money.of("50.00", currency);
    wallet.addBalance(initialAmount);

    walletService.addBalance(wallet, addAmount);

    Money expectedBalance = Money.of("150.00", currency);
    assertEquals(expectedBalance, walletService.getBalance(wallet, currency));
    verify(walletRepository).save(wallet);
  }

  @Test
  void shouldSubtractBalanceSuccessfully() {
    String currency = "USD";
    Money initialAmount = Money.of("100.00", currency);
    Money subtractAmount = Money.of("30.00", currency);
    wallet.addBalance(initialAmount);

    walletService.subtractBalance(wallet, subtractAmount);

    Money expectedBalance = Money.of("70.00", currency);
    assertEquals(expectedBalance, walletService.getBalance(wallet, currency));
    verify(walletRepository).save(wallet);
  }

  @Test
  void shouldThrowExceptionWhenSubtractingMoreThanAvailable() {
    String currency = "USD";
    Money initialAmount = Money.of("30.00", currency);
    Money subtractAmount = Money.of("50.00", currency);
    wallet.addBalance(initialAmount);

    assertThrows(
        IllegalArgumentException.class,
        () -> walletService.subtractBalance(wallet, subtractAmount));

    assertEquals(initialAmount, walletService.getBalance(wallet, currency));
    verify(walletRepository, never()).save(wallet);
  }

  @Test
  void shouldThrowExceptionWhenSubtractingFromZeroBalance() {
    String currency = "USD";
    Money subtractAmount = Money.of("10.00", currency);

    assertThrows(
        IllegalArgumentException.class,
        () -> walletService.subtractBalance(wallet, subtractAmount));

    verify(walletRepository, never()).save(wallet);
  }

  @Test
  void shouldSubtractExactBalanceSuccessfully() {
    String currency = "USD";
    Money balance = Money.of("50.00", currency);
    wallet.addBalance(balance);

    walletService.subtractBalance(wallet, balance);

    assertEquals(Money.zero(currency), walletService.getBalance(wallet, currency));
    verify(walletRepository).save(wallet);
  }
}
