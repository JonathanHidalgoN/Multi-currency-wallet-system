package com.payflow.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.payflow.entity.Transaction;
import com.payflow.entity.User;
import com.payflow.entity.Wallet;
import com.payflow.repository.ITransactionRepository;
import com.payflow.value.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction service tests")
class TransactionServiceTest {

  @Mock
  private ITransactionRepository transactionRepository;

  @Mock
  private WalletService walletService;

  @InjectMocks
  private TransactionService transactionService;

  private User user;
  private User recipientUser;
  private Wallet wallet;
  private Wallet recipientWallet;
  private Transaction transaction;

  @BeforeEach
  void setUp() {
    user = User.builder()
        .id(1L)
        .email("sender@example.com")
        .password("encoded_password")
        .fullName("Sender User")
        .enabled(true)
        .build();

    recipientUser = User.builder()
        .id(2L)
        .email("recipient@example.com")
        .password("encoded_password")
        .fullName("Recipient User")
        .enabled(true)
        .build();

    wallet = Wallet.builder()
        .id(1L)
        .user(user)
        .build();

    recipientWallet = Wallet.builder()
        .id(2L)
        .user(recipientUser)
        .build();

    transaction = Transaction.builder()
        .id(1L)
        .transactionId("TXN-1234567890-abcd1234")
        .wallet(wallet)
        .type(Transaction.TransactionType.DEPOSIT)
        .status(Transaction.TransactionStatus.COMPLETED)
        .amount(new BigDecimal("100.00"))
        .currency("USD")
        .createdAt(LocalDateTime.now())
        .completedAt(LocalDateTime.now())
        .build();
  }

  @Test
  void shouldDepositWithVariousAmounts() {
    BigDecimal[] amounts = {
        new BigDecimal("0.01"), // small
        new BigDecimal("123.45"), // decimal
        new BigDecimal("999999.99") // large
    };

    when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
      Transaction txn = invocation.getArgument(0);
      return txn;
    });

    for (BigDecimal amount : amounts) {
      Transaction result = transactionService.deposit(wallet, "USD", amount);

      assertNotNull(result);
      assertEquals(amount, result.getAmount());
      assertEquals("USD", result.getCurrency());
      assertEquals(Transaction.TransactionType.DEPOSIT, result.getType());
      assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());
      assertEquals(wallet.getId(), result.getWallet().getId());
      assertNotNull(result.getTransactionId());
      assertTrue(result.getTransactionId().startsWith("TXN-"));
    }

    verify(walletService, times(3)).addBalance(eq(wallet), any(Money.class));
    verify(transactionRepository, times(3)).save(any(Transaction.class));
  }

  @Test
  void shouldThrowExceptionWhenDepositAmountIsNull() {
    String currency = "USD";
    BigDecimal amount = null;

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.deposit(wallet, currency, amount));

    verify(transactionRepository, never()).save(any());
    verify(walletService, never()).addBalance(any(), any());
  }

  @Test
  void shouldThrowExceptionWhenDepositAmountIsZero() {
    String currency = "USD";
    BigDecimal amount = BigDecimal.ZERO;

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.deposit(wallet, currency, amount));

    verify(transactionRepository, never()).save(any());
    verify(walletService, never()).addBalance(any(), any());
  }

  @Test
  void shouldWithdrawWithVariousAmounts() {
    BigDecimal[] amounts = {
        new BigDecimal("0.01"), // small
        new BigDecimal("50.50"), // decimal
        new BigDecimal("500000.99") // large
    };

    when(walletService.hasSufficientBalance(eq(wallet), any(Money.class))).thenReturn(true);
    when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
      Transaction txn = invocation.getArgument(0);
      return txn;
    });

    for (BigDecimal amount : amounts) {
      Transaction result = transactionService.withdraw(wallet, "USD", amount);

      assertNotNull(result);
      assertEquals(amount, result.getAmount());
      assertEquals("USD", result.getCurrency());
      assertEquals(Transaction.TransactionType.WITHDRAWAL, result.getType());
      assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());
      assertEquals(wallet.getId(), result.getWallet().getId());
      assertNotNull(result.getTransactionId());
      assertTrue(result.getTransactionId().startsWith("TXN-"));
    }

    verify(walletService, times(3)).hasSufficientBalance(eq(wallet), any(Money.class));
    verify(walletService, times(3)).subtractBalance(eq(wallet), any(Money.class));
    verify(transactionRepository, times(3)).save(any(Transaction.class));
  }

  @Test
  void shouldThrowExceptionWhenWithdrawalAmountIsNull() {
    String currency = "USD";
    BigDecimal amount = null;

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.withdraw(wallet, currency, amount));

    verify(transactionRepository, never()).save(any());
    verify(walletService, never()).subtractBalance(any(), any());
  }

  @Test
  void shouldThrowExceptionWhenInsufficientBalance() {
    String currency = "USD";
    BigDecimal amount = new BigDecimal("100.00");

    when(walletService.hasSufficientBalance(eq(wallet), any(Money.class))).thenReturn(false);

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.withdraw(wallet, currency, amount));

    verify(walletService).hasSufficientBalance(eq(wallet), any(Money.class));
    verify(walletService, never()).subtractBalance(any(), any());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void shouldGetTransactionByIdSuccessfully() {
    String transactionId = "TXN-1234567890-abcd1234";
    when(transactionRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(transaction));

    Optional<Transaction> result = transactionService.getTransactionById(transactionId);

    assertTrue(result.isPresent());
    assertEquals(transactionId, result.get().getTransactionId());
    verify(transactionRepository).findByTransactionId(transactionId);
  }

  @Test
  void shouldReturnEmptyOptionalWhenTransactionNotFound() {
    String transactionId = "TXN-nonexistent";
    when(transactionRepository.findByTransactionId(transactionId)).thenReturn(Optional.empty());

    Optional<Transaction> result = transactionService.getTransactionById(transactionId);

    assertFalse(result.isPresent());
    assertTrue(result.isEmpty());
    verify(transactionRepository).findByTransactionId(transactionId);
  }

  @Test
  void shouldGetTransactionHistoryWithPagination() {
    Pageable pageable = PageRequest.of(0, 10);
    List<Transaction> transactions = Arrays.asList(transaction);
    Page<Transaction> page = new PageImpl<>(transactions, pageable, 1);

    when(transactionRepository.findByWallet(wallet, pageable)).thenReturn(page);

    Page<Transaction> result = transactionService.getTransactionHistory(wallet, pageable);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(1, result.getContent().size());
    assertEquals(transaction.getTransactionId(), result.getContent().get(0).getTransactionId());

    verify(transactionRepository).findByWallet(wallet, pageable);
  }

  @Test
  void shouldReturnEmptyPageWhenWalletHasNoTransactions() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Transaction> page = new PageImpl<>(Arrays.asList(), pageable, 0);

    when(transactionRepository.findByWallet(wallet, pageable)).thenReturn(page);

    Page<Transaction> result = transactionService.getTransactionHistory(wallet, pageable);

    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
    assertTrue(result.getContent().isEmpty());

    verify(transactionRepository).findByWallet(wallet, pageable);
  }

  @Test
  void shouldGetTransactionHistoryWithMultiplePages() {
    Pageable pageable = PageRequest.of(0, 10);
    List<Transaction> transactions = Arrays.asList(transaction, transaction);
    Page<Transaction> page = new PageImpl<>(transactions, pageable, 20);

    when(transactionRepository.findByWallet(wallet, pageable)).thenReturn(page);

    Page<Transaction> result = transactionService.getTransactionHistory(wallet, pageable);

    assertNotNull(result);
    assertEquals(20, result.getTotalElements());
    assertEquals(2, result.getContent().size());
    assertTrue(result.hasNext());

    verify(transactionRepository).findByWallet(wallet, pageable);
  }

  @Test
  void shouldTransferWithVariousAmountsAndCurrencies() {
    Object[][] testCases = {
        { new BigDecimal("0.01"), new BigDecimal("1.0"), "USD", "USD" }, // small same currency
        { new BigDecimal("100.50"), new BigDecimal("0.92"), "USD", "EUR" }, // medium with conversion
        { new BigDecimal("999999.99"), new BigDecimal("1.35"), "EUR", "GBP" } // large with conversion
    };

    when(walletService.getWalletByUser(user)).thenReturn(wallet);
    when(walletService.getWalletByUser(recipientUser)).thenReturn(recipientWallet);
    when(walletService.hasSufficientBalance(any(Wallet.class), any(Money.class))).thenReturn(true);
    when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
      Transaction txn = invocation.getArgument(0);
      return txn;
    });

    for (Object[] testCase : testCases) {
      BigDecimal amount = (BigDecimal) testCase[0];
      BigDecimal exchangeRate = (BigDecimal) testCase[1];
      String senderCurrency = (String) testCase[2];
      String recipientCurrency = (String) testCase[3];

      Transaction result = transactionService.transfer(
          user, recipientUser, senderCurrency, recipientCurrency, amount, exchangeRate);

      assertNotNull(result);
      assertEquals(amount, result.getAmount());
      assertEquals(senderCurrency, result.getCurrency());
      assertEquals(recipientCurrency, result.getRecipientCurrency());
      assertEquals(exchangeRate, result.getExchangeRate());
      assertEquals(Transaction.TransactionType.TRANSFER, result.getType());
      assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());
      assertEquals(wallet.getId(), result.getWallet().getId());
      assertEquals(recipientUser.getId(), result.getRecipientUser().getId());

      BigDecimal expectedFee = amount.multiply(new BigDecimal("0.015"));
      assertEquals(expectedFee, result.getFee());

      assertNotNull(result.getTransactionId());
      assertTrue(result.getTransactionId().startsWith("TXN-"));
    }

    verify(walletService, times(3)).hasSufficientBalance(any(Wallet.class), any(Money.class));
    verify(walletService, times(3)).subtractBalance(any(Wallet.class), any(Money.class));
    verify(walletService, times(3)).addBalance(any(Wallet.class), any(Money.class));
    verify(transactionRepository, times(3)).save(any(Transaction.class));
  }

  @Test
  void shouldThrowExceptionWhenTransferAmountIsNull() {
    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.transfer(user, recipientUser, "USD", "USD", null, BigDecimal.ONE));

    verify(transactionRepository, never()).save(any());
  }

  @Test
  void shouldThrowExceptionWhenTransferAmountIsZero() {
    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.transfer(user, recipientUser, "USD", "USD", BigDecimal.ZERO, BigDecimal.ONE));

    verify(transactionRepository, never()).save(any());
  }

  @Test
  void shouldThrowExceptionWhenTransferToSelf() {
    BigDecimal amount = new BigDecimal("100.00");

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.transfer(user, user, "USD", "USD", amount, BigDecimal.ONE));

    verify(walletService, never()).subtractBalance(any(), any());
    verify(walletService, never()).addBalance(any(), any());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void shouldThrowExceptionWhenInsufficientBalanceForTransfer() {
    BigDecimal amount = new BigDecimal("100.00");

    when(walletService.getWalletByUser(user)).thenReturn(wallet);
    when(walletService.getWalletByUser(recipientUser)).thenReturn(recipientWallet);
    when(walletService.hasSufficientBalance(eq(wallet), any(Money.class))).thenReturn(false);

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.transfer(user, recipientUser, "USD", "EUR", amount, new BigDecimal("0.92")));

    verify(walletService).hasSufficientBalance(eq(wallet), any(Money.class));
    verify(walletService, never()).subtractBalance(any(), any());
    verify(walletService, never()).addBalance(any(), any());
    verify(transactionRepository, never()).save(any());
  }
}
