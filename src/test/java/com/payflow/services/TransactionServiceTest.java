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
  void shouldDepositSuccessfully() {
    String currency = "USD";
    BigDecimal amount = new BigDecimal("100.00");

    when(transactionRepository.findByTransactionId(any())).thenReturn(Optional.empty());
    when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

    Transaction result = transactionService.deposit(wallet, currency, amount);

    assertNotNull(result);
    assertEquals(amount, result.getAmount());
    assertEquals(currency, result.getCurrency());
    assertEquals(Transaction.TransactionType.DEPOSIT, result.getType());
    assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());

    verify(walletService).addBalance(wallet, currency, amount);
    verify(transactionRepository).save(any(Transaction.class));
  }

  @Test
  void shouldThrowExceptionWhenDepositAmountIsNull() {
    String currency = "USD";
    BigDecimal amount = null;

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.deposit(wallet, currency, amount));

    verify(transactionRepository, never()).save(any());
    verify(walletService, never()).addBalance(any(), any(), any());
  }

  @Test
  void shouldThrowExceptionWhenDepositAmountIsZero() {
    String currency = "USD";
    BigDecimal amount = BigDecimal.ZERO;

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.deposit(wallet, currency, amount));

    verify(transactionRepository, never()).save(any());
    verify(walletService, never()).addBalance(any(), any(), any());
  }

  @Test
  void shouldDepositWithLargeAmount() {
    String currency = "USD";
    BigDecimal amount = new BigDecimal("999999.99");

    Transaction largeTransaction = Transaction.builder()
        .id(2L)
        .transactionId("TXN-large-deposit")
        .wallet(wallet)
        .type(Transaction.TransactionType.DEPOSIT)
        .status(Transaction.TransactionStatus.COMPLETED)
        .amount(amount)
        .currency(currency)
        .createdAt(LocalDateTime.now())
        .completedAt(LocalDateTime.now())
        .build();

    when(transactionRepository.findByTransactionId(any())).thenReturn(Optional.empty());
    when(transactionRepository.save(any(Transaction.class))).thenReturn(largeTransaction);

    Transaction result = transactionService.deposit(wallet, currency, amount);

    assertNotNull(result);
    assertEquals(amount, result.getAmount());

    verify(walletService).addBalance(wallet, currency, amount);
    verify(transactionRepository).save(any(Transaction.class));
  }

  @Test
  void shouldWithdrawSuccessfully() {
    String currency = "USD";
    BigDecimal amount = new BigDecimal("50.00");

    Transaction withdrawalTransaction = Transaction.builder()
        .id(3L)
        .transactionId("TXN-withdrawal")
        .wallet(wallet)
        .type(Transaction.TransactionType.WITHDRAWAL)
        .status(Transaction.TransactionStatus.COMPLETED)
        .amount(amount)
        .currency(currency)
        .createdAt(LocalDateTime.now())
        .completedAt(LocalDateTime.now())
        .build();

    when(walletService.hasSufficientBalance(wallet, currency, amount)).thenReturn(true);
    when(transactionRepository.save(any(Transaction.class))).thenReturn(withdrawalTransaction);

    Transaction result = transactionService.withdraw(wallet, currency, amount);

    assertNotNull(result);
    assertEquals(amount, result.getAmount());
    assertEquals(currency, result.getCurrency());
    assertEquals(Transaction.TransactionType.WITHDRAWAL, result.getType());
    assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());

    verify(walletService).hasSufficientBalance(wallet, currency, amount);
    verify(walletService).subtractBalance(wallet, currency, amount);
    verify(transactionRepository).save(any(Transaction.class));
  }

  @Test
  void shouldThrowExceptionWhenWithdrawalAmountIsNull() {
    String currency = "USD";
    BigDecimal amount = null;

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.withdraw(wallet, currency, amount));

    verify(transactionRepository, never()).save(any());
    verify(walletService, never()).subtractBalance(any(), any(), any());
  }

  @Test
  void shouldThrowExceptionWhenInsufficientBalance() {
    String currency = "USD";
    BigDecimal amount = new BigDecimal("100.00");

    when(walletService.hasSufficientBalance(wallet, currency, amount)).thenReturn(false);

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.withdraw(wallet, currency, amount));

    verify(walletService).hasSufficientBalance(wallet, currency, amount);
    verify(walletService, never()).subtractBalance(any(), any(), any());
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
}
