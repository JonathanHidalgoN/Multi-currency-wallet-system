package com.payflow.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "users")
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

  @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private Set<Role> roles = new HashSet<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Wallet getWallet() {
    return wallet;
  }

  public void setWallet(Wallet wallet) {
    this.wallet = wallet;
  }

  public java.util.List<Transaction> getReceivedTransactions() {
    return receivedTransactions;
  }

  public void setReceivedTransactions(java.util.List<Transaction> receivedTransactions) {
    this.receivedTransactions = receivedTransactions;
  }

  public Set<Role> getRoles() {
    return roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }

  public static UserBuilder builder() {
    return new UserBuilder();
  }

  public static class UserBuilder {
    private Long id;
    private String email;
    private String password;
    private String fullName;
    private Boolean enabled = true;
    private LocalDateTime updatedAt;
    private Wallet wallet;
    private java.util.List<Transaction> receivedTransactions;
    private Set<Role> roles = new HashSet<>();

    public UserBuilder id(Long id) {
      this.id = id;
      return this;
    }

    public UserBuilder email(String email) {
      this.email = email;
      return this;
    }

    public UserBuilder password(String password) {
      this.password = password;
      return this;
    }

    public UserBuilder fullName(String fullName) {
      this.fullName = fullName;
      return this;
    }

    public UserBuilder enabled(Boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public UserBuilder updatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    public UserBuilder wallet(Wallet wallet) {
      this.wallet = wallet;
      return this;
    }

    public UserBuilder receivedTransactions(java.util.List<Transaction> receivedTransactions) {
      this.receivedTransactions = receivedTransactions;
      return this;
    }

    public UserBuilder roles(Set<Role> roles) {
      this.roles = roles;
      return this;
    }

    public User build() {
      User user = new User();
      user.id = this.id;
      user.email = this.email;
      user.password = this.password;
      user.fullName = this.fullName;
      user.enabled = this.enabled;
      user.updatedAt = this.updatedAt;
      user.wallet = this.wallet;
      user.receivedTransactions = this.receivedTransactions;
      user.roles = this.roles;
      return user;
    }
  }
}
