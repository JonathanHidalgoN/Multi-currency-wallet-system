package com.payflow.services;

import com.payflow.entity.Transaction;
import com.payflow.entity.Wallet;
import com.payflow.entity.User;
import com.payflow.repository.ITransactionRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class TransactionService {

  private final ITransactionRepository transactionRepository;
  private final WalletService walletService;

  public TransactionService(
      ITransactionRepository transactionRepository,
      WalletService walletService,
      UserService userService) {
    this.transactionRepository = transactionRepository;
    this.walletService = walletService;
  }

  private String generateTransactionId() {
    return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
  }

  private void validateAmount(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ONE) < 0) {
      throw new IllegalArgumentException("Amount must be at least $1");
    }
  }

  public Transaction deposit(Wallet wallet, String currency, BigDecimal amount) {
    validateAmount(amount);

    String transactionId = generateTransactionId();

    Optional<Transaction> existing = transactionRepository.findByTransactionId(transactionId);
    if (existing.isPresent()) {
      return existing.get();
    }

    walletService.addBalance(wallet, currency, amount);

    Transaction transaction = Transaction.builder()
        .transactionId(transactionId)
        .wallet(wallet)
        .type(Transaction.TransactionType.DEPOSIT)
        .status(Transaction.TransactionStatus.COMPLETED)
        .amount(amount)
        .currency(currency)
        .createdAt(LocalDateTime.now())
        .completedAt(LocalDateTime.now())
        .build();

    return transactionRepository.save(transaction);
  }

  public Transaction withdraw(Wallet wallet, String currency, BigDecimal amount) {
    validateAmount(amount);

    if (!walletService.hasSufficientBalance(wallet, currency, amount)) {
      throw new IllegalArgumentException("Insufficient balance");
    }

    String transactionId = generateTransactionId();

    walletService.subtractBalance(wallet, currency, amount);

    Transaction transaction = Transaction.builder()
        .transactionId(transactionId)
        .wallet(wallet)
        .type(Transaction.TransactionType.WITHDRAWAL)
        .status(Transaction.TransactionStatus.COMPLETED)
        .amount(amount)
        .currency(currency)
        .createdAt(LocalDateTime.now())
        .completedAt(LocalDateTime.now())
        .build();

    return transactionRepository.save(transaction);
  }

  public Transaction transfer(
      User senderUser,
      User recipientUser,
      String senderCurrency,
      String recipientCurrency,
      BigDecimal amount,
      BigDecimal exchangeRate) {
    validateAmount(amount);

    if (recipientUser == null) {
      throw new IllegalArgumentException("Recipient not found");
    }

    if (senderUser.getId().equals(recipientUser.getId())) {
      throw new IllegalArgumentException("Cannot transfer to yourself");
    }

    Wallet senderWallet = walletService.getWalletByUser(senderUser);
    Wallet recipientWallet = walletService.getWalletByUser(recipientUser);

    BigDecimal fee = amount.multiply(new BigDecimal("0.015"));
    BigDecimal totalDebit = amount.add(fee);

    if (!walletService.hasSufficientBalance(senderWallet, senderCurrency, totalDebit)) {
      throw new IllegalArgumentException("Insufficient balance for transfer (including fee)");
    }

    String transactionId = generateTransactionId();

    walletService.subtractBalance(senderWallet, senderCurrency, totalDebit);

    BigDecimal convertedAmount = amount.multiply(exchangeRate);
    walletService.addBalance(recipientWallet, recipientCurrency, convertedAmount);

    Transaction transaction = Transaction.builder()
        .transactionId(transactionId)
        .wallet(senderWallet)
        .type(Transaction.TransactionType.TRANSFER)
        .status(Transaction.TransactionStatus.COMPLETED)
        .amount(amount)
        .currency(senderCurrency)
        .fee(fee)
        .recipientCurrency(recipientCurrency)
        .exchangeRate(exchangeRate)
        .recipientUser(recipientUser)
        .createdAt(LocalDateTime.now())
        .completedAt(LocalDateTime.now())
        .build();

    return transactionRepository.save(transaction);
  }

  public Optional<Transaction> getTransactionById(String transactionId) {
    return transactionRepository.findByTransactionId(transactionId);
  }

  public Page<Transaction> getTransactionHistory(Wallet wallet, Pageable pageable) {
    return transactionRepository.findByWallet(wallet, pageable);
  }
}
