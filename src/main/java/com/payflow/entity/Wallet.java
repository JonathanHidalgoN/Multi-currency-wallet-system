package com.payflow.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.payflow.value.Money;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "wallets")
public class Wallet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "wallet_balances", joinColumns = @JoinColumn(name = "wallet_id"))
  @MapKeyColumn(name = "currency")
  @Column(name = "balance")
  private Map<String, BigDecimal> balances = new HashMap<>();

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
  private java.util.List<Transaction> transactions;

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Map<String, BigDecimal> getBalances() {
    return balances;
  }

  public void setBalances(Map<String, BigDecimal> balances) {
    this.balances = balances;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public java.util.List<Transaction> getTransactions() {
    return transactions;
  }

  public void setTransactions(java.util.List<Transaction> transactions) {
    this.transactions = transactions;
  }

  public static WalletBuilder builder() {
    return new WalletBuilder();
  }

  public static class WalletBuilder {
    private Long id;
    private User user;
    private Map<String, BigDecimal> balances = new HashMap<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private java.util.List<Transaction> transactions;

    public WalletBuilder id(Long id) {
      this.id = id;
      return this;
    }

    public WalletBuilder user(User user) {
      this.user = user;
      return this;
    }

    public WalletBuilder balances(Map<String, BigDecimal> balances) {
      this.balances = balances;
      return this;
    }

    public WalletBuilder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public WalletBuilder updatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    public WalletBuilder transactions(java.util.List<Transaction> transactions) {
      this.transactions = transactions;
      return this;
    }

    public Wallet build() {
      Wallet wallet = new Wallet();
      wallet.id = this.id;
      wallet.user = this.user;
      wallet.balances = this.balances != null ? this.balances : new HashMap<>();
      wallet.createdAt = this.createdAt;
      wallet.updatedAt = this.updatedAt;
      wallet.transactions = this.transactions;
      return wallet;
    }
  }

  public Money getBalance(String currency) {
    BigDecimal amount = balances.getOrDefault(currency, BigDecimal.ZERO);
    return Money.of(amount, currency);
  }

  public void addBalance(Money money) {
    if (money == null) {
      throw new IllegalArgumentException("Money cannot be null");
    }
    Money current = getBalance(money.getCurrency());
    balances.put(money.getCurrency(), current.add(money).getAmount());
  }

  public void subtractBalance(Money money) {
    if (money == null) {
      throw new IllegalArgumentException("Money cannot be null");
    }
    Money current = getBalance(money.getCurrency());
    if (current.isLessThan(money)) {
      throw new IllegalArgumentException("Insufficient balance");
    }
    balances.put(money.getCurrency(), current.subtract(money).getAmount());
  }

  public boolean hasSufficientBalance(Money money) {
    if (money == null) {
      throw new IllegalArgumentException("Money cannot be null");
    }
    return getBalance(money.getCurrency()).isGreaterThanOrEqual(money);
  }
}
