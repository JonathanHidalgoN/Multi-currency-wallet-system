package com.payflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

  @ManyToOne(optional = false)
  @JoinColumn(name = "recipient_user_id", nullable = false)
  private User recipientUser;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime completedAt;

  @Column(name = "failure_reason", nullable = true)
  private String failureReason;

  /**
   * Transaction Types
   */
  public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER
  }

  /**
   * Transaction Status
   */
  public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED
  }
}
