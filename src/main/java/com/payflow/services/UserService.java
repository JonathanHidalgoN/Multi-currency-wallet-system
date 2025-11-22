package com.payflow.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.payflow.entity.User;
import com.payflow.repository.IUserRepository;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class UserService {

  private final IUserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final WalletService walletService;

  public UserService(IUserRepository userRepository,
      PasswordEncoder passwordEncoder,
      WalletService walletService) {
    this.userRepository = userRepository;
    this.walletService = walletService;
    this.passwordEncoder = passwordEncoder;
  }

  public User registerUser(String email, String password, String fullName) {
    if (userRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("Email already exists");
    }

    User user = User.builder()
        .email(email)
        .password(passwordEncoder.encode(password))
        .fullName(fullName)
        .enabled(true)
        .build();

    User savedUser = userRepository.save(user);
    walletService.createWalletForUser(savedUser);
    return savedUser;
  }

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public Optional<User> findById(Long userId) {
    return userRepository.findById(userId);
  }

  public User getUserById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  public boolean emailExists(String email) {
    return userRepository.existsByEmail(email);
  }
}
