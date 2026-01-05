package com.payflow.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.payflow.dto.v1.request.UserFilter;
import com.payflow.entity.Role;
import com.payflow.entity.User;
import com.payflow.exception.DuplicateEmailException;
import com.payflow.exception.UnauthorizedException;
import com.payflow.repository.IRoleRepository;
import com.payflow.repository.IUserRepository;
import com.payflow.specification.UserSpecification;

import jakarta.transaction.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  private final IUserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final WalletService walletService;
  private final IRoleRepository roleRepository;

  public UserService(IUserRepository userRepository,
      PasswordEncoder passwordEncoder,
      WalletService walletService,
      IRoleRepository roleRepository) {
    this.userRepository = userRepository;
    this.walletService = walletService;
    this.passwordEncoder = passwordEncoder;
    this.roleRepository = roleRepository;
  }

  public User registerUser(String email, String password, String fullName) {
    logger.info("User registration attempt for email: {}", email);

    if (userRepository.existsByEmail(email)) {
      logger.warn("Registration failed: email already exists: {}", email);
      throw new DuplicateEmailException(email);
    }

    Role userRole = roleRepository.findByRole("USER")
        .orElseThrow(() -> new IllegalStateException("USER role not found in database"));

    Set<Role> userSetRole = new HashSet<>();
    userSetRole.add(userRole);

    User user = User.builder()
        .email(email)
        .password(passwordEncoder.encode(password))
        .fullName(fullName)
        .enabled(true)
        .roles(userSetRole)
        .build();

    User savedUser = userRepository.save(user);
    logger.debug("User saved to database with ID: {}", savedUser.getId());

    walletService.createWalletForUser(savedUser);
    logger.info("User registered successfully - ID: {}, Email: {}", savedUser.getId(), email);

    return savedUser;
  }

  public Optional<User> findByEmail(String email) {
    logger.debug("Searching for user by email: {}", email);
    Optional<User> user = userRepository.findByEmail(email);
    if (user.isPresent()) {
      logger.debug("User found by email: {}, User ID: {}", email, user.get().getId());
    } else {
      logger.debug("No user found with email: {}", email);
    }
    return user;
  }

  public Optional<User> findById(Long userId) {
    logger.debug("Searching for user by ID: {}", userId);
    Optional<User> user = userRepository.findById(userId);
    if (user.isPresent()) {
      logger.debug("User found by ID: {}, Email: {}", userId, user.get().getEmail());
    } else {
      logger.debug("No user found with ID: {}", userId);
    }
    return user;
  }

  public User getUserById(Long userId) {
    logger.debug("Fetching user by ID: {}", userId);
    return userRepository.findById(userId)
        .orElseThrow(() -> {
          logger.warn("User not found with ID: {}", userId);
          return new IllegalArgumentException("User not found");
        });
  }

  public boolean emailExists(String email) {
    logger.debug("Checking if email exists: {}", email);
    boolean exists = userRepository.existsByEmail(email);
    logger.debug("Email existence check - Email: {}, Exists: {}", email, exists);
    return exists;
  }

  public User authenticate(String email, String password) {
    logger.info("Authentication attempt for email: {}", email);

    User user = findByEmail(email)
        .orElseThrow(() -> {
          logger.warn("Authentication failed: user not found for email: {}", email);
          return new UnauthorizedException("Invalid credentials");
        });

    if (!passwordEncoder.matches(password, user.getPassword())) {
      logger.warn("Authentication failed: invalid password for email: {}", email);
      throw new UnauthorizedException("Invalid credentials");
    }

    logger.info("Authentication successful for user ID: {}, email: {}", user.getId(), email);
    return user;
  }

  public Page<User> getUsers(UserFilter filter, Pageable pageable) {
    logger.info("Getting users with filters: email={}, fullName={}, enabled={}, fromDate={}, toDate={}, roleName={}",
        filter.email(), filter.fullName(), filter.enabled(), filter.fromDate(), filter.toDate(), filter.roleName());

    Specification<User> spec = UserSpecification.buildSpec(filter);
    return userRepository.findAll(spec, pageable);
  }

  public void disableUser(long userId) {
    logger.info("Disabling user with id: {}", userId);
    User user = findById(userId)
        .orElseThrow(() -> {
          logger.warn("User not found with ID: {}", userId);
          return new IllegalArgumentException("User not found");
        });
    user.setEnabled(false);
    userRepository.save(user);
    logger.info("Disabled user with id: {}", userId);
  }

  public void enableUser(long userId) {
    logger.info("Enabling user with id: {}", userId);
    User user = findById(userId)
        .orElseThrow(() -> {
          logger.warn("User not found with ID: {}", userId);
          return new IllegalArgumentException("User not found");
        });
    user.setEnabled(true);
    userRepository.save(user);
    logger.info("Enabled user with id: {}", userId);
  }

  public void updateUserRoles(Long userId, Set<String> roleNames) {
    logger.info("Updating roles for user ID: {}", userId);
    User user = getUserById(userId);

    Set<Role> roles = roleNames.stream()
        .map(roleName -> roleRepository.findByRole(roleName)
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName)))
        .collect(Collectors.toSet());

    user.setRoles(roles);
    userRepository.save(user);
    logger.info("Roles updated successfully for user ID: {}", userId);
  }

}
