package com.payflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

  public BigDecimal getBalance(String currency) {
    return balances.getOrDefault(currency, BigDecimal.ZERO);
  }

  public void addBalance(String currency, BigDecimal amount) {
    BigDecimal current = getBalance(currency);
    balances.put(currency, current.add(amount));
  }

  public void subtractBalance(String currency, BigDecimal amount) {
    BigDecimal current = getBalance(currency);
    balances.put(currency, current.subtract(amount));
  }
}
