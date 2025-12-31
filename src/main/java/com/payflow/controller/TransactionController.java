package com.payflow.controller;

import com.payflow.DTOS.DepositRequest;
import com.payflow.DTOS.TransactionDTO;
import com.payflow.DTOS.TransactionFilter;
import com.payflow.DTOS.TransactionResponse;
import com.payflow.DTOS.TransferRequest;
import com.payflow.DTOS.TransferResponse;
import com.payflow.DTOS.WithdrawRequest;
import com.payflow.entity.Transaction;
import com.payflow.entity.User;
import com.payflow.entity.Wallet;
import com.payflow.services.ExchangeRateService;
import com.payflow.services.TransactionService;
import com.payflow.services.UserService;
import com.payflow.services.WalletService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@PreAuthorize("isAuthenticated()")
public class TransactionController {

  private final TransactionService transactionService;
  private final WalletService walletService;
  private final UserService userService;
  private final ExchangeRateService exchangeRateService;

  public TransactionController(TransactionService transactionService,
      WalletService walletService, UserService userService,
      ExchangeRateService exchangeRateService) {
    this.transactionService = transactionService;
    this.walletService = walletService;
    this.userService = userService;
    this.exchangeRateService = exchangeRateService;
  }

  private void validateIdempotencyKey(String idempotencyKey) {
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      throw new IllegalArgumentException("Idempotency-Key header is required and cannot be blank");
    }
  }

  @PostMapping("/deposit")
  public ResponseEntity<TransactionResponse> deposit(
      Authentication authentication,
      @Valid @RequestBody DepositRequest request,
      @RequestHeader("Idempotency-Key") String idempotencyKey) {

    validateIdempotencyKey(idempotencyKey);

    User user = userService.getUserById(Long.parseLong(authentication.getName()));

    Transaction transaction = transactionService.deposit(
        user,
        request.currency(),
        request.amount(),
        idempotencyKey);

    TransactionResponse response = new TransactionResponse(
        transaction.getTransactionId(),
        transaction.getType().toString(),
        transaction.getAmount(),
        transaction.getCurrency(),
        transaction.getStatus().toString(),
        transaction.getCreatedAt());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/withdraw")
  public ResponseEntity<TransactionResponse> withdraw(
      Authentication authentication,
      @Valid @RequestBody WithdrawRequest request,
      @RequestHeader("Idempotency-Key") String idempotencyKey) {

    validateIdempotencyKey(idempotencyKey);

    User user = userService.getUserById(Long.parseLong(authentication.getName()));

    Transaction transaction = transactionService.withdraw(
        user,
        request.currency(),
        request.amount(),
        idempotencyKey);

    TransactionResponse response = new TransactionResponse(
        transaction.getTransactionId(),
        transaction.getType().toString(),
        transaction.getAmount(),
        transaction.getCurrency(),
        transaction.getStatus().toString(),
        transaction.getCreatedAt());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/transfer")
  public ResponseEntity<TransferResponse> transfer(
      Authentication authentication,
      @Valid @RequestBody TransferRequest request,
      @RequestHeader("Idempotency-Key") String idempotencyKey) {

    validateIdempotencyKey(idempotencyKey);

    User sender = userService.getUserById(Long.parseLong(authentication.getName()));
    User recipient = userService.getUserById(request.recipientUserId());

    BigDecimal exchangeRate = exchangeRateService.getExchangeRate(
        request.senderCurrency(),
        request.recipientCurrency());

    Transaction transaction = transactionService.transfer(
        sender,
        recipient,
        request.senderCurrency(),
        request.recipientCurrency(),
        request.amount(),
        exchangeRate,
        idempotencyKey);

    TransferResponse response = new TransferResponse(
        transaction.getTransactionId(),
        transaction.getType().toString(),
        transaction.getCurrency(),
        transaction.getAmount(),
        transaction.getFee(),
        transaction.getAmount().add(transaction.getFee()),
        transaction.getRecipientCurrency(),
        transaction.getAmount().multiply(exchangeRate),
        exchangeRate,
        transaction.getStatus().toString(),
        transaction.getCreatedAt());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/history")
  public ResponseEntity<Page<TransactionDTO>> getTransactionHistory(
      Authentication authentication,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
      @RequestParam(required = false) String currency,
      @RequestParam(required = false) Transaction.TransactionType type,
      @RequestParam(required = false) Transaction.TransactionStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate toDate,
      @RequestParam(required = false) BigDecimal minAmount,
      @RequestParam(required = false) BigDecimal maxAmount) {

    User user = userService.getUserById(Long.parseLong(authentication.getName()));
    Wallet wallet = walletService.getWalletByUserReadOnly(user);

    TransactionFilter filter = new TransactionFilter(
        currency,
        type,
        status,
        fromDate,
        toDate,
        minAmount,
        maxAmount);

    Page<Transaction> transactions = transactionService.getTransactionHistory(wallet, filter, pageable);

    Page<TransactionDTO> dtoPage = transactions.map(t -> new TransactionDTO(
        t.getTransactionId(),
        t.getType().toString(),
        t.getAmount(),
        t.getCurrency(),
        t.getStatus().toString(),
        t.getCreatedAt()));

    return ResponseEntity.ok(dtoPage);
  }
}
