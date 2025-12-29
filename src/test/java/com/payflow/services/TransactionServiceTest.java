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
import com.payflow.repository.IWalletRepository;
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

  @Mock
  private IWalletRepository walletRepository;

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
        new BigDecimal("0.01"),
        new BigDecimal("123.45"),
        new BigDecimal("999999.99")
    };

    when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
    when(walletRepository.findByUserIdWithLock(user.getId())).thenReturn(Optional.of(wallet));
    when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
      Transaction txn = invocation.getArgument(0);
      return txn;
    });

    for (int i = 0; i < amounts.length; i++) {
      BigDecimal amount = amounts[i];
      Transaction result = transactionService.deposit(user, "USD", amount, "idempotency-key-" + i);

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

    when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.deposit(user, currency, amount, "test-idempotency-key"));

    verify(transactionRepository, never()).save(any());
    verify(walletService, never()).addBalance(any(), any());
  }

  @Test
  void shouldThrowExceptionWhenDepositAmountIsZero() {
    String currency = "USD";
    BigDecimal amount = BigDecimal.ZERO;

    when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.deposit(user, currency, amount, "test-idempotency-key"));

    verify(transactionRepository, never()).save(any());
    verify(walletService, never()).addBalance(any(), any());
  }

  @Test
  void shouldWithdrawWithVariousAmounts() {
    BigDecimal[] amounts = {
        new BigDecimal("0.01"),
        new BigDecimal("50.50"),
        new BigDecimal("500000.99")
    };

    when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
    when(walletRepository.findByUserIdWithLock(user.getId())).thenReturn(Optional.of(wallet));
    when(walletService.hasSufficientBalance(eq(wallet), any(Money.class))).thenReturn(true);
    when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
      Transaction txn = invocation.getArgument(0);
      return txn;
    });

    for (int i = 0; i < amounts.length; i++) {
      BigDecimal amount = amounts[i];
      Transaction result = transactionService.withdraw(user, "USD", amount, "withdraw-key-" + i);

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

    when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.withdraw(user, currency, amount, "test-key"));

    verify(transactionRepository, never()).save(any());
    verify(walletService, never()).subtractBalance(any(), any());
  }

  @Test
  void shouldThrowExceptionWhenInsufficientBalance() {
    String currency = "USD";
    BigDecimal amount = new BigDecimal("100.00");

    when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
    when(walletRepository.findByUserIdWithLock(user.getId())).thenReturn(Optional.of(wallet));
    when(walletService.hasSufficientBalance(eq(wallet), any(Money.class))).thenReturn(false);

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.withdraw(user, currency, amount, "test-key"));

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
        { new BigDecimal("0.01"), new BigDecimal("1.0"), "USD", "USD" },
        { new BigDecimal("100.50"), new BigDecimal("0.92"), "USD", "EUR" },
        { new BigDecimal("999999.99"), new BigDecimal("1.35"), "EUR", "GBP" }
    };

    when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
    when(walletRepository.findByUserIdWithLock(user.getId())).thenReturn(Optional.of(wallet));
    when(walletRepository.findByUserIdWithLock(recipientUser.getId())).thenReturn(Optional.of(recipientWallet));
    when(walletService.hasSufficientBalance(any(Wallet.class), any(Money.class))).thenReturn(true);
    when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
      Transaction txn = invocation.getArgument(0);
      return txn;
    });

    for (int i = 0; i < testCases.length; i++) {
      Object[] testCase = testCases[i];
      BigDecimal amount = (BigDecimal) testCase[0];
      BigDecimal exchangeRate = (BigDecimal) testCase[1];
      String senderCurrency = (String) testCase[2];
      String recipientCurrency = (String) testCase[3];

      Transaction result = transactionService.transfer(
          user, recipientUser, senderCurrency, recipientCurrency, amount, exchangeRate, "transfer-key-" + i);

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
    when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.transfer(user, recipientUser, "USD", "USD", null, BigDecimal.ONE, "test-key"));

    verify(transactionRepository, never()).save(any());
  }

  @Test
  void shouldThrowExceptionWhenTransferAmountIsZero() {
    when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.transfer(user, recipientUser, "USD", "USD", BigDecimal.ZERO, BigDecimal.ONE,
            "test-key"));

    verify(transactionRepository, never()).save(any());
  }

  @Test
  void shouldThrowExceptionWhenTransferToSelf() {
    BigDecimal amount = new BigDecimal("100.00");

    when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.transfer(user, user, "USD", "USD", amount, BigDecimal.ONE, "test-key"));

    verify(walletService, never()).subtractBalance(any(), any());
    verify(walletService, never()).addBalance(any(), any());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void shouldThrowExceptionWhenInsufficientBalanceForTransfer() {
    BigDecimal amount = new BigDecimal("100.00");

    when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
    when(walletRepository.findByUserIdWithLock(user.getId())).thenReturn(Optional.of(wallet));
    when(walletRepository.findByUserIdWithLock(recipientUser.getId())).thenReturn(Optional.of(recipientWallet));
    when(walletService.hasSufficientBalance(eq(wallet), any(Money.class))).thenReturn(false);

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.transfer(user, recipientUser, "USD", "EUR", amount, new BigDecimal("0.92"),
            "test-key"));

    verify(walletService).hasSufficientBalance(eq(wallet), any(Money.class));
    verify(walletService, never()).subtractBalance(any(), any());
    verify(walletService, never()).addBalance(any(), any());
    verify(transactionRepository, never()).save(any());
  }

  @Test
  void shouldReturnExistingTransactionWhenDepositWithDuplicateIdempotencyKey() {
    String idempotencyKey = "duplicate-deposit-key";
    Transaction existingTransaction = Transaction.builder()
        .id(100L)
        .transactionId("TXN-EXISTING-1234")
        .wallet(wallet)
        .type(Transaction.TransactionType.DEPOSIT)
        .status(Transaction.TransactionStatus.COMPLETED)
        .amount(new BigDecimal("100.00"))
        .currency("USD")
        .idempotencyKey(idempotencyKey)
        .createdAt(LocalDateTime.now())
        .completedAt(LocalDateTime.now())
        .build();

    when(transactionRepository.findByIdempotencyKey(idempotencyKey))
        .thenReturn(Optional.of(existingTransaction));

    Transaction result = transactionService.deposit(user, "USD", new BigDecimal("100.00"), idempotencyKey);

    assertNotNull(result);
    assertEquals(existingTransaction.getTransactionId(), result.getTransactionId());
    assertEquals(existingTransaction.getId(), result.getId());
    assertEquals("TXN-EXISTING-1234", result.getTransactionId());

    verify(transactionRepository, never()).save(any());
    verify(walletService, never()).addBalance(any(), any());
    verify(transactionRepository).findByIdempotencyKey(idempotencyKey);
  }

  @Test
  void shouldReturnExistingTransactionWhenWithdrawWithDuplicateIdempotencyKey() {
    String idempotencyKey = "duplicate-withdraw-key";
    Transaction existingTransaction = Transaction.builder()
        .id(200L)
        .transactionId("TXN-EXISTING-5678")
        .wallet(wallet)
        .type(Transaction.TransactionType.WITHDRAWAL)
        .status(Transaction.TransactionStatus.COMPLETED)
        .amount(new BigDecimal("50.00"))
        .currency("USD")
        .idempotencyKey(idempotencyKey)
        .createdAt(LocalDateTime.now())
        .completedAt(LocalDateTime.now())
        .build();

    when(transactionRepository.findByIdempotencyKey(idempotencyKey))
        .thenReturn(Optional.of(existingTransaction));

    Transaction result = transactionService.withdraw(user, "USD", new BigDecimal("50.00"), idempotencyKey);

    assertNotNull(result);
    assertEquals(existingTransaction.getTransactionId(), result.getTransactionId());
    assertEquals(existingTransaction.getId(), result.getId());
    assertEquals("TXN-EXISTING-5678", result.getTransactionId());

    verify(transactionRepository, never()).save(any());
    verify(walletService, never()).hasSufficientBalance(any(), any());
    verify(walletService, never()).subtractBalance(any(), any());
    verify(transactionRepository).findByIdempotencyKey(idempotencyKey);
  }

  @Test
  void shouldReturnExistingTransactionWhenTransferWithDuplicateIdempotencyKey() {
    String idempotencyKey = "duplicate-transfer-key";
    Transaction existingTransaction = Transaction.builder()
        .id(300L)
        .transactionId("TXN-EXISTING-9999")
        .wallet(wallet)
        .type(Transaction.TransactionType.TRANSFER)
        .status(Transaction.TransactionStatus.COMPLETED)
        .amount(new BigDecimal("100.00"))
        .currency("USD")
        .fee(new BigDecimal("1.50"))
        .recipientCurrency("EUR")
        .exchangeRate(new BigDecimal("0.92"))
        .recipientUser(recipientUser)
        .idempotencyKey(idempotencyKey)
        .createdAt(LocalDateTime.now())
        .completedAt(LocalDateTime.now())
        .build();

    when(transactionRepository.findByIdempotencyKey(idempotencyKey))
        .thenReturn(Optional.of(existingTransaction));

    Transaction result = transactionService.transfer(
        user,
        recipientUser,
        "USD",
        "EUR",
        new BigDecimal("100.00"),
        new BigDecimal("0.92"),
        idempotencyKey);

    assertNotNull(result);
    assertEquals(existingTransaction.getTransactionId(), result.getTransactionId());
    assertEquals(existingTransaction.getId(), result.getId());
    assertEquals("TXN-EXISTING-9999", result.getTransactionId());

    verify(transactionRepository, never()).save(any());
    verify(walletService, never()).getWalletByUserReadOnly(any());
    verify(walletService, never()).hasSufficientBalance(any(), any());
    verify(walletService, never()).subtractBalance(any(), any());
    verify(walletService, never()).addBalance(any(), any());
    verify(transactionRepository).findByIdempotencyKey(idempotencyKey);
  }

  @Test
  void shouldCreateNewTransactionWhenIdempotencyKeyIsUnique() {
    String uniqueKey = "unique-deposit-key";

    when(transactionRepository.findByIdempotencyKey(uniqueKey)).thenReturn(Optional.empty());
    when(walletRepository.findByUserIdWithLock(user.getId())).thenReturn(Optional.of(wallet));
    when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
      Transaction txn = invocation.getArgument(0);
      txn.setId(999L);
      return txn;
    });

    Transaction result = transactionService.deposit(user, "USD", new BigDecimal("100.00"), uniqueKey);

    assertNotNull(result);
    assertNotNull(result.getTransactionId());

    verify(transactionRepository).findByIdempotencyKey(uniqueKey);
    verify(transactionRepository).save(any(Transaction.class));
    verify(walletService).addBalance(eq(wallet), any(Money.class));
  }
}
