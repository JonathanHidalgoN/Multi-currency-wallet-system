package com.payflow.controller;

import com.payflow.entity.User;
import com.payflow.entity.Wallet;
import com.payflow.services.UserService;
import com.payflow.services.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

  private final WalletService walletService;
  private final UserService userService;

  public WalletController(WalletService walletService, UserService userService) {
    this.walletService = walletService;
    this.userService = userService;
  }

  @GetMapping("/me")
  public ResponseEntity<Map<String, Object>> getMyWallet(
      Authentication authentication) {

    User user = userService.getUserById(Long.parseLong(authentication.getName()));
    Wallet wallet = walletService.getWalletByUser(user);

    Map<String, Object> response = new HashMap<>();
    response.put("id", wallet.getId());
    response.put("userId", user.getId());
    response.put("balances", wallet.getBalances());
    response.put("createdAt", wallet.getCreatedAt());
    response.put("updatedAt", wallet.getUpdatedAt());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/me/balance")
  public ResponseEntity<Map<String, Object>> getBalance(
      Authentication authentication,
      @RequestParam String currency) {

    User user = userService.getUserById(Long.parseLong(authentication.getName()));
    Wallet wallet = walletService.getWalletByUser(user);

    BigDecimal balance = walletService.getBalance(wallet, currency);

    Map<String, Object> response = new HashMap<>();
    response.put("currency", currency);
    response.put("balance", balance);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/me/balances")
  public ResponseEntity<Map<String, Object>> getAllBalances(
      Authentication authentication) {

    User user = userService.getUserById(Long.parseLong(authentication.getName()));
    Wallet wallet = walletService.getWalletByUser(user);

    Map<String, Object> response = new HashMap<>();
    response.put("walletId", wallet.getId());
    response.put("balances", wallet.getBalances());

    return ResponseEntity.ok(response);
  }
}
