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

import java.math.BigDecimal;
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
    when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

    Wallet result = walletService.getWalletByUser(user);

    assertNotNull(result);
    assertEquals(wallet.getId(), result.getId());
    verify(walletRepository).findByUser(user);
  }

  @Test
  void shouldThrowExceptionWhenWalletNotFoundByUser() {
    when(walletRepository.findByUser(user)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> walletService.getWalletByUser(user));

    verify(walletRepository).findByUser(user);
  }

  @Test
  void shouldReturnWalletWhenFoundByUserId() {
    Long userId = 1L;
    when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

    Wallet result = walletService.getWalletByUserId(userId);

    assertNotNull(result);
    assertEquals(wallet.getId(), result.getId());
    verify(walletRepository).findByUserId(userId);
  }

  @Test
  void shouldThrowExceptionWhenWalletNotFoundByUserId() {
    Long userId = 999L;
    when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> walletService.getWalletByUserId(userId));

    verify(walletRepository).findByUserId(userId);
  }

  @Test
  void shouldReturnBalanceForGivenCurrency() {
    String currency = "USD";
    BigDecimal expectedBalance = new BigDecimal("100.00");
    wallet.addBalance(currency, expectedBalance);

    BigDecimal result = walletService.getBalance(wallet, currency);

    assertEquals(expectedBalance, result);
  }

  @Test
  void shouldReturnTrueWhenSufficientBalance() {
    String currency = "USD";
    BigDecimal balance = new BigDecimal("100.00");
    BigDecimal requestedAmount = new BigDecimal("50.00");
    wallet.addBalance(currency, balance);

    boolean result = walletService.hasSufficientBalance(wallet, currency, requestedAmount);

    assertTrue(result);
  }

  @Test
  void shouldReturnFalseWhenInsufficientBalance() {
    String currency = "USD";
    BigDecimal balance = new BigDecimal("30.00");
    BigDecimal requestedAmount = new BigDecimal("50.00");
    wallet.addBalance(currency, balance);

    boolean result = walletService.hasSufficientBalance(wallet, currency, requestedAmount);

    assertFalse(result);
  }

  @Test
  void shouldReturnTrueWhenBalanceEqualsRequestedAmount() {
    String currency = "USD";
    BigDecimal balance = new BigDecimal("50.00");
    wallet.addBalance(currency, balance);

    boolean result = walletService.hasSufficientBalance(wallet, currency, balance);

    assertTrue(result);
  }

  @Test
  void shouldAddBalanceSuccessfully() {
    String currency = "USD";
    BigDecimal initialAmount = new BigDecimal("100.00");
    BigDecimal addAmount = new BigDecimal("50.00");
    wallet.addBalance(currency, initialAmount);

    walletService.addBalance(wallet, currency, addAmount);

    BigDecimal expectedBalance = new BigDecimal("150.00");
    assertEquals(expectedBalance, wallet.getBalance(currency));
    verify(walletRepository).save(wallet);
  }

  @Test
  void shouldSubtractBalanceSuccessfully() {
    String currency = "USD";
    BigDecimal initialAmount = new BigDecimal("100.00");
    BigDecimal subtractAmount = new BigDecimal("30.00");
    wallet.addBalance(currency, initialAmount);

    walletService.subtractBalance(wallet, currency, subtractAmount);

    BigDecimal expectedBalance = new BigDecimal("70.00");
    assertEquals(expectedBalance, wallet.getBalance(currency));
    verify(walletRepository).save(wallet);
  }

  @Test
  void shouldThrowExceptionWhenSubtractingMoreThanAvailable() {
    String currency = "USD";
    BigDecimal initialAmount = new BigDecimal("30.00");
    BigDecimal subtractAmount = new BigDecimal("50.00");
    wallet.addBalance(currency, initialAmount);

    assertThrows(
        IllegalArgumentException.class,
        () -> walletService.subtractBalance(wallet, currency, subtractAmount));

    assertEquals(initialAmount, wallet.getBalance(currency));
    verify(walletRepository, never()).save(wallet);
  }

  @Test
  void shouldThrowExceptionWhenSubtractingFromZeroBalance() {
    String currency = "USD";
    BigDecimal subtractAmount = new BigDecimal("10.00");

    assertThrows(
        IllegalArgumentException.class,
        () -> walletService.subtractBalance(wallet, currency, subtractAmount));

    verify(walletRepository, never()).save(wallet);
  }

  @Test
  void shouldSubtractExactBalanceSuccessfully() {
    String currency = "USD";
    BigDecimal balance = new BigDecimal("50.00");
    wallet.addBalance(currency, balance);

    walletService.subtractBalance(wallet, currency, balance);

    assertEquals(0, wallet.getBalance(currency).compareTo(BigDecimal.ZERO));
    verify(walletRepository).save(wallet);
  }
}
