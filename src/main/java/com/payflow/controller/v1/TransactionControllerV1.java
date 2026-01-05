package com.payflow.controller.v1;

import com.payflow.dto.v1.request.DepositRequest;
import com.payflow.dto.v1.response.TransactionDTO;
import com.payflow.dto.v1.request.TransactionFilter;
import com.payflow.dto.v1.response.TransactionResponse;
import com.payflow.dto.v1.request.TransferRequest;
import com.payflow.dto.v1.response.TransferResponse;
import com.payflow.dto.v1.request.WithdrawRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/transactions")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Transactions (v1)", description = "Transaction management endpoints - Version 1")
public class TransactionControllerV1 {

  private final TransactionService transactionService;
  private final WalletService walletService;
  private final UserService userService;
  private final ExchangeRateService exchangeRateService;

  public TransactionControllerV1(TransactionService transactionService,
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
      @Valid @ModelAttribute TransactionFilter filter) {

    User user = userService.getUserById(Long.parseLong(authentication.getName()));
    Wallet wallet = walletService.getWalletByUserReadOnly(user);

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
