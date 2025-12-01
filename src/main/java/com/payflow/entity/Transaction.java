package com.payflow.entity;

import org.hibernate.annotations.CreationTimestamp;

import com.payflow.value.Money;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String transactionId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "wallet_id", nullable = false)
  private Wallet wallet;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransactionType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransactionStatus status;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency;

  @Column(precision = 10, scale = 6)
  private BigDecimal fee;

  @Column(length = 3)
  private String recipientCurrency;

  @Column(precision = 10, scale = 6)
  private BigDecimal exchangeRate;

  @ManyToOne(optional = true)
  @JoinColumn(name = "recipient_user_id", nullable = true)
  private User recipientUser;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime completedAt;

  @Column(name = "failure_reason", nullable = true)
  private String failureReason;

  @Column(name = "idempotency_key", unique = true)
  private String idempotencyKey;

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public Wallet getWallet() {
    return wallet;
  }

  public void setWallet(Wallet wallet) {
    this.wallet = wallet;
  }

  public TransactionType getType() {
    return type;
  }

  public void setType(TransactionType type) {
    this.type = type;
  }

  public TransactionStatus getStatus() {
    return status;
  }

  public void setStatus(TransactionStatus status) {
    this.status = status;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public BigDecimal getFee() {
    return fee;
  }

  public void setFee(BigDecimal fee) {
    this.fee = fee;
  }

  public String getRecipientCurrency() {
    return recipientCurrency;
  }

  public void setRecipientCurrency(String recipientCurrency) {
    this.recipientCurrency = recipientCurrency;
  }

  public BigDecimal getExchangeRate() {
    return exchangeRate;
  }

  public void setExchangeRate(BigDecimal exchangeRate) {
    this.exchangeRate = exchangeRate;
  }

  public User getRecipientUser() {
    return recipientUser;
  }

  public void setRecipientUser(User recipientUser) {
    this.recipientUser = recipientUser;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(LocalDateTime completedAt) {
    this.completedAt = completedAt;
  }

  public String getFailureReason() {
    return failureReason;
  }

  public void setFailureReason(String failureReason) {
    this.failureReason = failureReason;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public void setIdempotencyKey(String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
  }

  public static TransactionBuilder builder() {
    return new TransactionBuilder();
  }

  public static class TransactionBuilder {
    private Long id;
    private String transactionId;
    private Wallet wallet;
    private TransactionType type;
    private TransactionStatus status;
    private BigDecimal amount;
    private String currency;
    private BigDecimal fee;
    private String recipientCurrency;
    private BigDecimal exchangeRate;
    private User recipientUser;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String failureReason;
    private String idempotencyKey;

    public TransactionBuilder id(Long id) {
      this.id = id;
      return this;
    }

    public TransactionBuilder transactionId(String transactionId) {
      this.transactionId = transactionId;
      return this;
    }

    public TransactionBuilder wallet(Wallet wallet) {
      this.wallet = wallet;
      return this;
    }

    public TransactionBuilder type(TransactionType type) {
      this.type = type;
      return this;
    }

    public TransactionBuilder status(TransactionStatus status) {
      this.status = status;
      return this;
    }

    public TransactionBuilder amount(BigDecimal amount) {
      this.amount = amount;
      return this;
    }

    public TransactionBuilder currency(String currency) {
      this.currency = currency;
      return this;
    }

    public TransactionBuilder fee(BigDecimal fee) {
      this.fee = fee;
      return this;
    }

    public TransactionBuilder recipientCurrency(String recipientCurrency) {
      this.recipientCurrency = recipientCurrency;
      return this;
    }

    public TransactionBuilder exchangeRate(BigDecimal exchangeRate) {
      this.exchangeRate = exchangeRate;
      return this;
    }

    public TransactionBuilder recipientUser(User recipientUser) {
      this.recipientUser = recipientUser;
      return this;
    }

    public TransactionBuilder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public TransactionBuilder completedAt(LocalDateTime completedAt) {
      this.completedAt = completedAt;
      return this;
    }

    public TransactionBuilder failureReason(String failureReason) {
      this.failureReason = failureReason;
      return this;
    }

    public TransactionBuilder idempotencyKey(String idempotencyKey) {
      this.idempotencyKey = idempotencyKey;
      return this;
    }

    public Transaction build() {
      Transaction transaction = new Transaction();
      transaction.id = this.id;
      transaction.transactionId = this.transactionId;
      transaction.wallet = this.wallet;
      transaction.type = this.type;
      transaction.status = this.status;
      transaction.amount = this.amount;
      transaction.currency = this.currency;
      transaction.fee = this.fee;
      transaction.recipientCurrency = this.recipientCurrency;
      transaction.exchangeRate = this.exchangeRate;
      transaction.recipientUser = this.recipientUser;
      transaction.createdAt = this.createdAt;
      transaction.completedAt = this.completedAt;
      transaction.failureReason = this.failureReason;
      transaction.idempotencyKey = this.idempotencyKey;
      return transaction;
    }
  }

  public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER
  }

  public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED
  }

  public Money getAmountMoney() {
    return Money.of(amount, currency);
  }

  public Money getFeeMoney() {
    if (fee == null) {
      return null;
    }
    return Money.of(fee, currency);
  }

  public Money getRecipientAmountMoney() {
    if (amount == null || recipientCurrency == null) {
      return null;
    }
    BigDecimal convertedAmount = amount.multiply(exchangeRate);
    return Money.of(convertedAmount, recipientCurrency);
  }
}
