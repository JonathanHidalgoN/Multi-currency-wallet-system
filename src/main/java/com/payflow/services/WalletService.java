package com.payflow.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.payflow.entity.User;
import com.payflow.entity.Wallet;
import com.payflow.repository.IWalletRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class WalletService {

  private final IWalletRepository walletRepository;

  public WalletService(IWalletRepository walletRepository) {
    this.walletRepository = walletRepository;
  }

  public Wallet createWalletForUser(User user) {
    Wallet wallet = Wallet.builder()
        .user(user)
        .build();
    return walletRepository.save(wallet);
  }

  public Wallet getWalletByUser(User user) {
    return walletRepository.findByUser(user)
        .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user"));
  }

  public Wallet getWalletByUserId(Long userId) {
    return walletRepository.findByUserId(userId)
        .orElseThrow(
            () -> new IllegalArgumentException("Wallet not found"));
  }

  public BigDecimal getBalance(Wallet wallet, String currency) {
    return wallet.getBalance(currency);
  }

  public boolean hasSufficientBalance(Wallet wallet, String currency,
      BigDecimal amount) {
    return wallet.getBalance(currency).compareTo(amount) >= 0;
  }

  public void addBalance(Wallet wallet, String currency, BigDecimal amount) {
    wallet.addBalance(currency, amount);
    walletRepository.save(wallet);
  }

  public void subtractBalance(Wallet wallet, String currency,
      BigDecimal amount) {
    if (!hasSufficientBalance(wallet, currency, amount)) {
      throw new IllegalArgumentException("Insufficient balance");
    }
    wallet.subtractBalance(currency, amount);
    walletRepository.save(wallet);
  }

}
