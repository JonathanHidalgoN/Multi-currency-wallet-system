package com.payflow.services;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.payflow.entity.User;
import com.payflow.entity.Wallet;
import com.payflow.repository.IWalletRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class WalletService {

  private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

  private final IWalletRepository walletRepository;

  public WalletService(IWalletRepository walletRepository) {
    this.walletRepository = walletRepository;
  }

  public Wallet createWalletForUser(User user) {
    logger.debug("Creating wallet for user ID: {}", user.getId());
    Wallet wallet = Wallet.builder()
        .user(user)
        .build();
    Wallet savedWallet = walletRepository.save(wallet);
    logger.info("Wallet created for user ID: {}, Wallet ID: {}", user.getId(), savedWallet.getId());
    return savedWallet;
  }

  public Wallet getWalletByUser(User user) {
    return walletRepository.findByUser(user)
        .orElseThrow(() -> {
          logger.warn("Wallet not found for user ID: {}", user.getId());
          return new IllegalArgumentException("Wallet not found for user");
        });
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
    logger.debug("Adding balance - Wallet ID: {}, Currency: {}, Amount: {}", wallet.getId(), currency, amount);
    wallet.addBalance(currency, amount);
    walletRepository.save(wallet);
    logger.debug("Balance added successfully - New balance: {}", wallet.getBalance(currency));
  }

  public void subtractBalance(Wallet wallet, String currency,
      BigDecimal amount) {
    logger.debug("Subtracting balance - Wallet ID: {}, Currency: {}, Amount: {}", wallet.getId(), currency, amount);
    if (!hasSufficientBalance(wallet, currency, amount)) {
      BigDecimal currentBalance = wallet.getBalance(currency);
      logger.warn("Insufficient balance - Wallet ID: {}, Currency: {}, Required: {}, Available: {}",
          wallet.getId(), currency, amount, currentBalance);
      throw new IllegalArgumentException("Insufficient balance");
    }
    wallet.subtractBalance(currency, amount);
    walletRepository.save(wallet);
    logger.debug("Balance subtracted successfully - New balance: {}", wallet.getBalance(currency));
  }

}
