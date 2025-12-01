package com.payflow.services;

import com.payflow.entity.Transaction;
import com.payflow.entity.Wallet;
import com.payflow.entity.User;
import com.payflow.repository.ITransactionRepository;
import com.payflow.value.Money;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

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
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Amount must be greater than zero");
    }
  }

  private Optional<Transaction> checkForDuplicateRequest(String idempotencyKey, String transactionType) {
    Optional<Transaction> existingTransaction = transactionRepository.findByIdempotencyKey(idempotencyKey);
    if (existingTransaction.isPresent()) {
      logger.info("Duplicate {} request detected - Returning existing transaction with Idempotency Key: {}",
          transactionType, idempotencyKey);
    }
    return existingTransaction;
  }

  public Transaction deposit(Wallet wallet, String currency, BigDecimal amount, String idempotencyKey) {
    logger.info("Deposit initiated - Wallet ID: {}, Currency: {}, Amount: {}, Idempotency Key: {}",
        wallet.getId(), currency, amount, idempotencyKey);

    Optional<Transaction> existingTransaction = checkForDuplicateRequest(idempotencyKey, "deposit");
    if (existingTransaction.isPresent()) {
      return existingTransaction.get();
    }

    validateAmount(amount);

    String transactionId = generateTransactionId();
    Money money = Money.of(amount, currency);

    walletService.addBalance(wallet, money);

    Transaction transaction = Transaction.builder()
        .transactionId(transactionId)
        .wallet(wallet)
        .type(Transaction.TransactionType.DEPOSIT)
        .status(Transaction.TransactionStatus.COMPLETED)
        .amount(amount)
        .currency(currency)
        .idempotencyKey(idempotencyKey)
        .createdAt(LocalDateTime.now())
        .completedAt(LocalDateTime.now())
        .build();

    Transaction savedTransaction = transactionRepository.save(transaction);
    logger.info("Deposit completed successfully - Transaction ID: {}, Amount: {} {}", transactionId, amount, currency);

    return savedTransaction;
  }

  public Transaction withdraw(Wallet wallet, String currency, BigDecimal amount, String idempotencyKey) {
    logger.info("Withdrawal initiated - Wallet ID: {}, Currency: {}, Amount: {}, Idempotency Key: {}",
        wallet.getId(), currency, amount, idempotencyKey);

    Optional<Transaction> existingTransaction = checkForDuplicateRequest(idempotencyKey, "withdrawal");
    if (existingTransaction.isPresent()) {
      return existingTransaction.get();
    }

    validateAmount(amount);

    String transactionId = generateTransactionId();
    Money money = Money.of(amount, currency);

    if (!walletService.hasSufficientBalance(wallet, money)) {
      logger.warn("Withdrawal rejected - Insufficient balance for Wallet ID: {}", wallet.getId());
      throw new IllegalArgumentException("Insufficient balance");
    }

    walletService.subtractBalance(wallet, money);

    Transaction transaction = Transaction.builder()
        .transactionId(transactionId)
        .wallet(wallet)
        .type(Transaction.TransactionType.WITHDRAWAL)
        .status(Transaction.TransactionStatus.COMPLETED)
        .amount(amount)
        .currency(currency)
        .idempotencyKey(idempotencyKey)
        .createdAt(LocalDateTime.now())
        .completedAt(LocalDateTime.now())
        .build();

    Transaction savedTransaction = transactionRepository.save(transaction);
    logger.info("Withdrawal completed successfully - Transaction ID: {}, Amount: {} {}", transactionId, amount,
        currency);

    return savedTransaction;
  }

  public Transaction transfer(
      User senderUser,
      User recipientUser,
      String senderCurrency,
      String recipientCurrency,
      BigDecimal amount,
      BigDecimal exchangeRate,
      String idempotencyKey) {
    logger.info(
        "Transfer initiated - Sender ID: {}, Recipient ID: {}, Amount: {} {}, Exchange Rate: {}, Idempotency Key: {}",
        senderUser.getId(), recipientUser.getId(), amount, senderCurrency, exchangeRate, idempotencyKey);

    Optional<Transaction> existingTransaction = checkForDuplicateRequest(idempotencyKey, "transfer");
    if (existingTransaction.isPresent()) {
      return existingTransaction.get();
    }

    validateAmount(amount);

    if (senderUser.getId().equals(recipientUser.getId())) {
      logger.warn("Transfer rejected - Sender cannot transfer to themselves, User ID: {}", senderUser.getId());
      throw new IllegalArgumentException("Cannot transfer to yourself");
    }

    Wallet senderWallet = walletService.getWalletByUser(senderUser);
    Wallet recipientWallet = walletService.getWalletByUser(recipientUser);

    Money moneyAmount = Money.of(amount, senderCurrency);
    BigDecimal fee = amount.multiply(new BigDecimal("0.015"));
    Money moneyFee = Money.of(fee, senderCurrency);
    Money totalDebit = moneyAmount.add(moneyFee);
    logger.debug("Transfer fee calculated - Amount: {}, Fee: {}, Total Debit: {}", moneyAmount, moneyFee, totalDebit);

    if (!walletService.hasSufficientBalance(senderWallet, totalDebit)) {
      logger.warn(
          "Transfer rejected - Insufficient balance including fee for Sender ID: {}. Required: {}, Available: {}",
          senderUser.getId(), totalDebit, walletService.getBalance(senderWallet, senderCurrency));
      throw new IllegalArgumentException("Insufficient balance for transfer (including fee)");
    }

    String transactionId = generateTransactionId();
    logger.debug("Transfer transaction ID generated: {}", transactionId);

    walletService.subtractBalance(senderWallet, totalDebit);

    BigDecimal convertedAmount = amount.multiply(exchangeRate);
    Money convertedMoney = Money.of(convertedAmount, recipientCurrency);
    logger.debug("Amount converted - Original: {}, Converted: {}", moneyAmount, convertedMoney);

    walletService.addBalance(recipientWallet, convertedMoney);

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
        .idempotencyKey(idempotencyKey)
        .createdAt(LocalDateTime.now())
        .completedAt(LocalDateTime.now())
        .build();

    Transaction savedTransaction = transactionRepository.save(transaction);
    logger.info(
        "Transfer completed successfully - Transaction ID: {}, Sender ID: {}, Recipient ID: {}, Amount: {} → {}",
        transactionId, senderUser.getId(), recipientUser.getId(), moneyAmount, convertedMoney);

    return savedTransaction;
  }

  public Optional<Transaction> getTransactionById(String transactionId) {
    return transactionRepository.findByTransactionId(transactionId);
  }

  public Page<Transaction> getTransactionHistory(Wallet wallet, Pageable pageable) {
    return transactionRepository.findByWallet(wallet, pageable);
  }
}
