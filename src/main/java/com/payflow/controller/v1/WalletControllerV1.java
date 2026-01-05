package com.payflow.controller.v1;

import com.payflow.dto.v1.response.BalanceResponse;
import com.payflow.dto.v1.response.BalancesResponse;
import com.payflow.dto.v1.response.FullWalletResponse;
import com.payflow.entity.User;
import com.payflow.entity.Wallet;
import com.payflow.services.UserService;
import com.payflow.services.WalletService;
import com.payflow.value.Money;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/wallets")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Wallets (v1)", description = "Wallet management endpoints - Version 1")
public class WalletControllerV1 {

  private final WalletService walletService;
  private final UserService userService;

  public WalletControllerV1(WalletService walletService, UserService userService) {
    this.walletService = walletService;
    this.userService = userService;
  }

  @GetMapping("/me")
  public ResponseEntity<FullWalletResponse> getMyWallet(
      Authentication authentication) {

    User user = userService.getUserById(Long.parseLong(authentication.getName()));
    Wallet wallet = walletService.getWalletByUserReadOnly(user);

    FullWalletResponse response = new FullWalletResponse(
        wallet.getId(),
        user.getId(),
        wallet.getBalances(),
        wallet.getCreatedAt(),
        wallet.getUpdatedAt());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/me/balance")
  public ResponseEntity<BalanceResponse> getBalance(
      Authentication authentication,
      @RequestParam String currency) {

    User user = userService.getUserById(Long.parseLong(authentication.getName()));
    Wallet wallet = walletService.getWalletByUserReadOnly(user);

    Money balance = walletService.getBalance(wallet, currency);

    BalanceResponse response = new BalanceResponse(currency, balance.getAmount());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/me/balances")
  public ResponseEntity<BalancesResponse> getAllBalances(
      Authentication authentication) {

    User user = userService.getUserById(Long.parseLong(authentication.getName()));
    Wallet wallet = walletService.getWalletByUserReadOnly(user);

    BalancesResponse response = new BalancesResponse(wallet.getId(), wallet.getBalances());

    return ResponseEntity.ok(response);
  }
}
