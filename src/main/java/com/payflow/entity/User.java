package com.payflow.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Email(message = "Email should be valid")
  @Column(nullable = false, unique = true)
  private String email;

  @NotBlank(message = "Password cannot be blank")
  @Column(nullable = false)
  private String password;

  @NotBlank(message = "fullName cannot be blank")
  @Column(nullable = false)
  private String fullName;

  @Column(nullable = false)
  private Boolean enabled = true;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime updatedAt;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Wallet wallet;

  @OneToMany(mappedBy = "recipientUser")
  private java.util.List<Transaction> receivedTransactions;
}
