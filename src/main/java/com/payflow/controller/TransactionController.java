package com.payflow.controller;

import com.payflow.DTOS.DepositRequest;
import com.payflow.DTOS.WithdrawRequest;
import com.payflow.DTOS.TransferRequest;
import com.payflow.entity.Transaction;
import com.payflow.entity.User;
import com.payflow.entity.Wallet;
import com.payflow.services.TransactionService;
import com.payflow.services.UserService;
import com.payflow.services.WalletService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

  private final TransactionService transactionService;
  private final WalletService walletService;
  private final UserService userService;

  public TransactionController(TransactionService transactionService,
      WalletService walletService, UserService userService) {
    this.transactionService = transactionService;
    this.walletService = walletService;
    this.userService = userService;
  }

  @PostMapping("/deposit")
  public ResponseEntity<Map<String, Object>> deposit(
      Authentication authentication,
      @Valid @RequestBody DepositRequest request) {

    User user = userService.getUserById(Long.parseLong(authentication.getName()));
    Wallet wallet = walletService.getWalletByUser(user);

    Transaction transaction = transactionService.deposit(
        wallet,
        request.currency(),
        request.amount());

    Map<String, Object> response = new HashMap<>();
    response.put("transactionId", transaction.getTransactionId());
    response.put("type", transaction.getType());
    response.put("amount", transaction.getAmount());
    response.put("currency", transaction.getCurrency());
    response.put("status", transaction.getStatus());
    response.put("timestamp", transaction.getCreatedAt());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/withdraw")
  public ResponseEntity<Map<String, Object>> withdraw(
      Authentication authentication,
      @Valid @RequestBody WithdrawRequest request) {

    User user = userService.getUserById(Long.parseLong(authentication.getName()));
    Wallet wallet = walletService.getWalletByUser(user);

    Transaction transaction = transactionService.withdraw(
        wallet,
        request.currency(),
        request.amount());

    Map<String, Object> response = new HashMap<>();
    response.put("transactionId", transaction.getTransactionId());
    response.put("type", transaction.getType());
    response.put("amount", transaction.getAmount());
    response.put("currency", transaction.getCurrency());
    response.put("status", transaction.getStatus());
    response.put("timestamp", transaction.getCreatedAt());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/transfer")
  public ResponseEntity<Map<String, Object>> transfer(
      Authentication authentication,
      @Valid @RequestBody TransferRequest request) {

    User sender = userService.getUserById(Long.parseLong(authentication.getName()));
    User recipient = userService.getUserById(request.recipientUserId());

    BigDecimal exchangeRate = new BigDecimal("1.0");
    if (!request.senderCurrency().equals(request.recipientCurrency())) {
      exchangeRate = getExchangeRate(request.senderCurrency(), request.recipientCurrency());
    }

    Transaction transaction = transactionService.transfer(
        sender,
        recipient,
        request.senderCurrency(),
        request.recipientCurrency(),
        request.amount(),
        exchangeRate);

    Map<String, Object> response = new HashMap<>();
    response.put("transactionId", transaction.getTransactionId());
    response.put("type", transaction.getType());
    response.put("senderCurrency", transaction.getCurrency());
    response.put("senderAmount", transaction.getAmount());
    response.put("fee", transaction.getFee());
    response.put("totalDebit", transaction.getAmount().add(transaction.getFee()));
    response.put("recipientCurrency", transaction.getRecipientCurrency());
    response.put("recipientAmount", transaction.getAmount().multiply(exchangeRate));
    response.put("exchangeRate", exchangeRate);
    response.put("status", transaction.getStatus());
    response.put("timestamp", transaction.getCreatedAt());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/history")
  public ResponseEntity<Page<Transaction>> getTransactionHistory(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    User user = userService.getUserById(Long.parseLong(authentication.getName()));
    Wallet wallet = walletService.getWalletByUser(user);

    Pageable pageable = PageRequest.of(page, size);
    Page<Transaction> transactions = transactionService.getTransactionHistory(wallet, pageable);

    return ResponseEntity.ok(transactions);
  }

  private BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
    Map<String, BigDecimal> rates = new HashMap<>();
    rates.put("USD-EUR", new BigDecimal("0.92"));
    rates.put("EUR-USD", new BigDecimal("1.09"));
    rates.put("USD-MXN", new BigDecimal("17.50"));
    rates.put("MXN-USD", new BigDecimal("0.057"));

    String key = fromCurrency + "-" + toCurrency;
    return rates.getOrDefault(key, BigDecimal.ONE);
  }
}
