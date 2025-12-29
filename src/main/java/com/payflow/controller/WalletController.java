package com.payflow.controller;

import com.payflow.DTOS.BalanceResponse;
import com.payflow.DTOS.BalancesResponse;
import com.payflow.DTOS.FullWalletResponse;
import com.payflow.entity.User;
import com.payflow.entity.Wallet;
import com.payflow.services.UserService;
import com.payflow.services.WalletService;
import com.payflow.value.Money;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@PreAuthorize("isAuthenticated()")
public class WalletController {

  private final WalletService walletService;
  private final UserService userService;

  public WalletController(WalletService walletService, UserService userService) {
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
