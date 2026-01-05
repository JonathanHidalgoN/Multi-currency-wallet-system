package com.payflow.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.payflow.entity.Role;
import com.payflow.entity.User;
import com.payflow.security.JwtTokenProvider;
import com.payflow.services.UserService;

import jakarta.transaction.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
@ActiveProfiles("test")
class WalletControllerIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  private final MockMvc mockMvc;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserService userService;

  private User user;
  private String validToken;

  @Autowired
  public WalletControllerIntegrationTest(
      final MockMvc mockMvc,
      final JwtTokenProvider jwtTokenProvider,
      final UserService userService) {
    this.mockMvc = mockMvc;
    this.jwtTokenProvider = jwtTokenProvider;
    this.userService = userService;
  }

  @BeforeEach
  void setUp() {
    user = userService.registerUser("test@email.com", "password123", "Test user");

    // Extract roles from user
    Set<String> roles = user.getRoles().stream()
        .map(Role::getRole)
        .collect(Collectors.toSet());

    validToken = jwtTokenProvider.generateToken(user.getId(), roles);
  }

  @Test
  void shouldGetWalletDetailsWithValidToken() throws Exception {
    mockMvc.perform(get("/api/v1/wallets/me")
        .header("Authorization", "Bearer " + validToken))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturn403WhenNoTokenProvided() throws Exception {
    mockMvc.perform(get("/api/v1/wallets/me"))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldReturn403WithInvalidToken() throws Exception {
    mockMvc.perform(get("/api/v1/wallets/me")
        .header("Authorization", "Bearer invalid.token.here"))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldGetBalanceForSpecificCurrency() throws Exception {
    mockMvc.perform(get("/api/v1/wallets/me/balance")
        .param("currency", "USD")
        .header("Authorization", "Bearer " + validToken))
        .andExpect(status().isOk());
  }

  @Test
  void shouldGetAllBalances() throws Exception {
    mockMvc.perform(get("/api/v1/wallets/me/balances")
        .header("Authorization", "Bearer " + validToken))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturn400WhenCurrencyParameterMissing() throws Exception {
    mockMvc.perform(get("/api/v1/wallets/me/balance")
        .header("Authorization", "Bearer " + validToken))
        .andExpect(status().isBadRequest());
  }

}
