package com.payflow.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.payflow.entity.User;
import com.payflow.entity.Wallet;
import com.payflow.repository.IWalletRepository;
import com.payflow.value.Money;

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
    logger.debug("Fetching wallet for user ID: {}", user.getId());
    return walletRepository.findByUser(user)
        .orElseThrow(() -> {
          logger.warn("Wallet not found for user ID: {}", user.getId());
          return new IllegalArgumentException("Wallet not found for user");
        });
  }

  public Wallet getWalletByUserId(Long userId) {
    logger.debug("Fetching wallet for user ID: {}", userId);
    return walletRepository.findByUserId(userId)
        .orElseThrow(() -> {
          logger.warn("Wallet not found for user ID: {}", userId);
          return new IllegalArgumentException("Wallet not found");
        });
  }

  public Money getBalance(Wallet wallet, String currency) {
    if (wallet == null) {
      throw new IllegalArgumentException("Wallet cannot be null");
    }
    if (currency == null || currency.trim().isEmpty()) {
      throw new IllegalArgumentException("Currency cannot be null or empty");
    }
    logger.debug("Retrieving balance - Wallet ID: {}, Currency: {}", wallet.getId(), currency);
    Money balance = wallet.getBalance(currency);
    logger.debug("Balance retrieved - Wallet ID: {}, Currency: {}, Amount: {}", wallet.getId(), currency, balance);
    return balance;
  }

  public boolean hasSufficientBalance(Wallet wallet, Money amount) {
    if (wallet == null) {
      throw new IllegalArgumentException("Wallet cannot be null");
    }
    if (amount == null) {
      throw new IllegalArgumentException("Amount cannot be null");
    }
    logger.debug("Checking sufficient balance - Wallet ID: {}, Required: {}", wallet.getId(), amount);
    boolean sufficient = wallet.hasSufficientBalance(amount);
    logger.debug("Balance check result - Wallet ID: {}, Required: {}, Sufficient: {}", wallet.getId(), amount, sufficient);
    return sufficient;
  }

  public void addBalance(Wallet wallet, Money amount) {
    if (wallet == null) {
      throw new IllegalArgumentException("Wallet cannot be null");
    }
    if (amount == null) {
      throw new IllegalArgumentException("Amount cannot be null");
    }
    logger.debug("Adding balance - Wallet ID: {}, Money: {}", wallet.getId(), amount);
    wallet.addBalance(amount);
    walletRepository.save(wallet);
    logger.debug("Balance added successfully - New balance: {}", wallet.getBalance(amount.getCurrency()));
  }

  public void subtractBalance(Wallet wallet, Money amount) {
    if (wallet == null) {
      throw new IllegalArgumentException("Wallet cannot be null");
    }
    if (amount == null) {
      throw new IllegalArgumentException("Amount cannot be null");
    }
    logger.debug("Subtracting balance - Wallet ID: {}, Money: {}", wallet.getId(), amount);
    Money currentBalance = wallet.getBalance(amount.getCurrency());
    if (!wallet.hasSufficientBalance(amount)) {
      logger.warn(
          "Insufficient balance - Wallet ID: {}, Currency: {}, Required: {}, Available: {}",
          wallet.getId(), amount.getCurrency(), amount, currentBalance);
      throw new IllegalArgumentException("Insufficient balance");
    }
    wallet.subtractBalance(amount);
    walletRepository.save(wallet);
    logger.debug("Balance subtracted successfully - New balance: {}", wallet.getBalance(amount.getCurrency()));
  }

}
