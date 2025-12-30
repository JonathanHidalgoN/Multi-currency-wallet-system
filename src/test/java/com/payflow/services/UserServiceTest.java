package com.payflow.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.payflow.entity.Role;
import com.payflow.entity.User;
import com.payflow.repository.IRoleRepository;
import com.payflow.repository.IUserRepository;

import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
@DisplayName("User service tests")
class UserServiceTest {

  @Mock
  private IUserRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private WalletService walletService;
  @Mock
  private IRoleRepository roleRepository;
  @InjectMocks
  private UserService userService;

  private User user;
  private Role userRole;

  @BeforeEach
  void setUp() {
    userRole = new Role("USER");
    userRole.setId(1L);

    user = User.builder()
        .id(1L)
        .email("test@example.com")
        .password("encoded_password")
        .fullName("Test user")
        .enabled(true)
        .build();
  }

  @Test
  void shouldRegisterUserSuccessfully() {

    String email = user.getEmail();
    String password = "password123";
    String fullName = user.getFullName();

    when(userRepository.existsByEmail(email)).thenReturn(false);
    when(passwordEncoder.encode(password)).thenReturn("encoded_password");
    when(roleRepository.findByRole("USER")).thenReturn(Optional.of(userRole));
    when(userRepository.save(any(User.class))).thenReturn(user);

    User result = userService.registerUser(email, password, fullName);

    assertEquals(email, result.getEmail());
    assertEquals(fullName, result.getFullName());
    assertEquals(user.getPassword(), result.getPassword());

    verify(userRepository).existsByEmail(email);
    verify(roleRepository).findByRole("USER");
    verify(userRepository).save(any());
    verify(walletService).createWalletForUser(any());

  }

  @Test
  void shouldThrowExceptionWhenEmailAlreadyExists() {
    String email = "existing@example.com";
    String password = "password123";
    String fullName = "Existing User";

    when(userRepository.existsByEmail(email)).thenReturn(true);

    assertThrows(
        Exception.class,
        () -> userService.registerUser(email, password, fullName));

    verify(userRepository).existsByEmail(email);
    verify(userRepository, never()).save(any());
    verify(walletService, never()).createWalletForUser(any());
  }

  @Test
  void shouldFindUserByEmailWhenExists() {
    String email = "test@example.com";
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    Optional<User> result = userService.findByEmail(email);

    assertTrue(result.isPresent());
    assertEquals(user.getEmail(), result.get().getEmail());
    assertEquals(user.getId(), result.get().getId());
  }

  @Test
  void shouldReturnEmptyWhenUserEmailNotFound() {
    String email = "nonexistent@example.com";
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    Optional<User> result = userService.findByEmail(email);

    assertFalse(result.isPresent());
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldFindUserByIdWhenExists() {
    Long userId = 1L;
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    Optional<User> result = userService.findById(userId);

    assertTrue(result.isPresent());
    assertEquals(user.getId(), result.get().getId());
    assertEquals(user.getEmail(), result.get().getEmail());
  }

  @Test
  void shouldReturnEmptyWhenUserIdNotFound() {
    Long userId = 999L;
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    Optional<User> result = userService.findById(userId);

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldThrowExceptionWhenGetUserByIdNotFound() {
    Long userId = 999L;
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        Exception.class,
        () -> userService.getUserById(userId));
  }

  @Test
  void shouldReturnTrueWhenEmailExists() {
    String email = "test@example.com";
    when(userRepository.existsByEmail(email)).thenReturn(true);

    boolean result = userService.emailExists(email);

    assertTrue(result);
  }

  @Test
  void shouldReturnFalseWhenEmailDoesNotExist() {
    String email = "nonexistent@example.com";
    when(userRepository.existsByEmail(email)).thenReturn(false);

    boolean result = userService.emailExists(email);

    assertFalse(result);
  }

  @Test
  void shouldAuthenticateUserWithValidCredentials() {
    String email = "test@example.com";
    String password = "password123";

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

    User result = userService.authenticate(email, password);

    assertNotNull(result);
    assertEquals(user.getId(), result.getId());
    assertEquals(user.getEmail(), result.getEmail());
    assertEquals(user.getFullName(), result.getFullName());

    verify(userRepository).findByEmail(email);
    verify(passwordEncoder).matches(password, user.getPassword());
  }

  @Test
  void shouldThrowUnauthorizedExceptionWhenUserNotFound() {
    String email = "nonexistent@example.com";
    String password = "password123";

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    Exception exception = assertThrows(
        Exception.class,
        () -> userService.authenticate(email, password));

    assertTrue(exception.getMessage().contains("Invalid credentials"));

    verify(userRepository).findByEmail(email);
    verify(passwordEncoder, never()).matches(any(), any());
  }

  @Test
  void shouldThrowUnauthorizedExceptionWhenPasswordIncorrect() {
    String email = "test@example.com";
    String password = "wrongPassword";

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);

    Exception exception = assertThrows(
        Exception.class,
        () -> userService.authenticate(email, password));

    assertTrue(exception.getMessage().contains("Invalid credentials"));

    verify(userRepository).findByEmail(email);
    verify(passwordEncoder).matches(password, user.getPassword());
  }

  @Test
  void shouldDisableUserSuccessfully() {
    Long userId = 1L;
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    userService.disableUser(userId);

    verify(userRepository).findById(userId);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void shouldThrowExceptionWhenDisablingNonExistentUser() {
    Long userId = 999L;
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> userService.disableUser(userId));

    assertTrue(exception.getMessage().contains("User not found"));
    verify(userRepository).findById(userId);
    verify(userRepository, never()).save(any());
  }

  @Test
  void shouldEnableUserSuccessfully() {
    Long userId = 1L;
    user.setEnabled(false);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    userService.enableUser(userId);

    verify(userRepository).findById(userId);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void shouldThrowExceptionWhenEnablingNonExistentUser() {
    Long userId = 999L;
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> userService.enableUser(userId));

    assertTrue(exception.getMessage().contains("User not found"));
    verify(userRepository).findById(userId);
    verify(userRepository, never()).save(any());
  }

  @Test
  void shouldUpdateUserRolesSuccessfully() {
    Long userId = 1L;
    Role adminRole = new Role("ADMIN");
    adminRole.setId(2L);
    Role auditorRole = new Role("AUDITOR");
    auditorRole.setId(3L);

    Set<String> roleNames = Set.of("ADMIN", "AUDITOR");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(roleRepository.findByRole("ADMIN")).thenReturn(Optional.of(adminRole));
    when(roleRepository.findByRole("AUDITOR")).thenReturn(Optional.of(auditorRole));
    when(userRepository.save(any(User.class))).thenReturn(user);

    userService.updateUserRoles(userId, roleNames);

    verify(userRepository).findById(userId);
    verify(roleRepository).findByRole("ADMIN");
    verify(roleRepository).findByRole("AUDITOR");
    verify(userRepository).save(any(User.class));
  }

  @Test
  void shouldThrowExceptionWhenUpdatingRolesForNonExistentUser() {
    Long userId = 999L;
    Set<String> roleNames = Set.of("ADMIN");

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> userService.updateUserRoles(userId, roleNames));

    assertTrue(exception.getMessage().contains("User not found"));
    verify(userRepository).findById(userId);
    verify(roleRepository, never()).findByRole(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  void shouldThrowExceptionWhenUpdatingWithNonExistentRole() {
    Long userId = 1L;
    Set<String> roleNames = Set.of("INVALID_ROLE");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(roleRepository.findByRole("INVALID_ROLE")).thenReturn(Optional.empty());

    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> userService.updateUserRoles(userId, roleNames));

    assertTrue(exception.getMessage().contains("Role not found"));
    verify(userRepository).findById(userId);
    verify(roleRepository).findByRole("INVALID_ROLE");
    verify(userRepository, never()).save(any());
  }

}
