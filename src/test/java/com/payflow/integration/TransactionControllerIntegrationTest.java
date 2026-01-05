package com.payflow.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.dto.v1.request.DepositRequest;
import com.payflow.dto.v1.request.LoginRequest;
import com.payflow.dto.v1.request.RegisterRequest;
import com.payflow.dto.v1.request.TransferRequest;
import com.payflow.dto.v1.request.WithdrawRequest;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
@ActiveProfiles("test")
class TransactionControllerIntegrationTest {

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
  private final ObjectMapper objectMapper;

  @Autowired
  public TransactionControllerIntegrationTest(
      final MockMvc mockMvc,
      final ObjectMapper objectMapper) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
  }

  private String userToken;
  private Long userId;
  private String secondUserToken;
  private Long secondUserId;

  @BeforeEach
  void setUp() throws Exception {
    MvcResult user1Result = registerAndLogin("user1@example.com", "Password123!", "User One");
    String user1Response = user1Result.getResponse().getContentAsString();
    userToken = objectMapper.readTree(user1Response).get("token").asText();
    userId = objectMapper.readTree(user1Response).get("id").asLong();

    MvcResult user2Result = registerAndLogin("user2@example.com", "Password123!", "User Two");
    String user2Response = user2Result.getResponse().getContentAsString();
    secondUserToken = objectMapper.readTree(user2Response).get("token").asText();
    secondUserId = objectMapper.readTree(user2Response).get("id").asLong();
  }

  private MvcResult registerAndLogin(String email, String password, String fullName) throws Exception {
    RegisterRequest registerRequest = new RegisterRequest(email, password, fullName);

    mockMvc.perform(post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated());

    LoginRequest loginRequest = new LoginRequest(email, password);

    return mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andReturn();
  }

  @Test
  void shouldDepositSuccessfully() throws Exception {
    DepositRequest request = new DepositRequest(new BigDecimal("100.00"), "USD");

    mockMvc.perform(post("/api/v1/transactions/deposit")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", "deposit-test-1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  @Test
  void shouldWithdrawSuccessfully() throws Exception {
    DepositRequest depositRequest = new DepositRequest(new BigDecimal("200.00"), "USD");
    mockMvc.perform(post("/api/v1/transactions/deposit")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", "withdraw-test-setup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(depositRequest)))
        .andExpect(status().isCreated());

    WithdrawRequest withdrawRequest = new WithdrawRequest(new BigDecimal("50.00"), "USD");

    mockMvc.perform(post("/api/v1/transactions/withdraw")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", "withdraw-test-1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(withdrawRequest)))
        .andExpect(status().isCreated());
  }

  @Test
  void shouldFailWithdrawWhenInsufficientFunds() throws Exception {
    WithdrawRequest request = new WithdrawRequest(new BigDecimal("1000.00"), "USD");

    mockMvc.perform(post("/api/v1/transactions/withdraw")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", "insufficient-funds-test")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldTransferSuccessfully() throws Exception {
    DepositRequest depositRequest = new DepositRequest(new BigDecimal("500.00"), "USD");
    mockMvc.perform(post("/api/v1/transactions/deposit")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", "transfer-test-setup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(depositRequest)))
        .andExpect(status().isCreated());

    TransferRequest transferRequest = new TransferRequest(
        secondUserId,
        "USD",
        "USD",
        new BigDecimal("100.00"));

    mockMvc.perform(post("/api/v1/transactions/transfer")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", "transfer-test-1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(transferRequest)))
        .andExpect(status().isCreated());
  }

  @Test
  void shouldGetTransactionHistory() throws Exception {
    DepositRequest depositRequest = new DepositRequest(new BigDecimal("100.00"), "USD");
    mockMvc.perform(post("/api/v1/transactions/deposit")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", "history-test-setup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(depositRequest)))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/v1/transactions/history")
        .header("Authorization", "Bearer " + userToken)
        .param("page", "0")
        .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void shouldReturn403WhenNotAuthenticated() throws Exception {
    DepositRequest request = new DepositRequest(new BigDecimal("100.00"), "USD");

    mockMvc.perform(post("/api/v1/transactions/deposit")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldReturn400WhenDepositWithInvalidAmount() throws Exception {
    DepositRequest request = new DepositRequest(new BigDecimal("-50.00"), "USD");

    mockMvc.perform(post("/api/v1/transactions/deposit")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", "invalid-amount-test")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenWithdrawWithInvalidAmount() throws Exception {
    WithdrawRequest request = new WithdrawRequest(new BigDecimal("0"), "USD");

    mockMvc.perform(post("/api/v1/transactions/withdraw")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", "invalid-withdraw-amount-test")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldPaginateTransactionHistory() throws Exception {
    for (int i = 0; i < 15; i++) {
      DepositRequest depositRequest = new DepositRequest(new BigDecimal("10.00"), "USD");
      mockMvc.perform(post("/api/v1/transactions/deposit")
          .header("Authorization", "Bearer " + userToken)
          .header("Idempotency-Key", "pagination-test-" + i)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(depositRequest)))
          .andExpect(status().isCreated());
    }

    mockMvc.perform(get("/api/v1/transactions/history")
        .header("Authorization", "Bearer " + userToken)
        .param("page", "0")
        .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(10))
        .andExpect(jsonPath("$.totalElements").value(15))
        .andExpect(jsonPath("$.totalPages").value(2));

    mockMvc.perform(get("/api/v1/transactions/history")
        .header("Authorization", "Bearer " + userToken)
        .param("page", "1")
        .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(5));
  }

  @Test
  void shouldReturnSameTransactionForDuplicateDepositWithSameIdempotencyKey() throws Exception {
    DepositRequest request = new DepositRequest(new BigDecimal("100.00"), "USD");
    String idempotencyKey = "deposit-idempotency-test-1";

    MvcResult firstResult = mockMvc.perform(post("/api/v1/transactions/deposit")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", idempotencyKey)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andReturn();

    String firstTransactionId = objectMapper.readTree(firstResult.getResponse().getContentAsString())
        .get("transactionId").asText();

    MvcResult secondResult = mockMvc.perform(post("/api/v1/transactions/deposit")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", idempotencyKey)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andReturn();

    String secondTransactionId = objectMapper.readTree(secondResult.getResponse().getContentAsString())
        .get("transactionId").asText();

    assertEquals(firstTransactionId, secondTransactionId);
  }

  @Test
  void shouldReturnSameTransactionForDuplicateWithdrawWithSameIdempotencyKey() throws Exception {
    DepositRequest depositRequest = new DepositRequest(new BigDecimal("500.00"), "USD");
    mockMvc.perform(post("/api/v1/transactions/deposit")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", "setup-deposit-for-withdraw-idempotency-test")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(depositRequest)))
        .andExpect(status().isCreated());

    WithdrawRequest request = new WithdrawRequest(new BigDecimal("100.00"), "USD");
    String idempotencyKey = "withdraw-idempotency-test-1";

    MvcResult firstResult = mockMvc.perform(post("/api/v1/transactions/withdraw")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", idempotencyKey)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andReturn();

    String firstTransactionId = objectMapper.readTree(firstResult.getResponse().getContentAsString())
        .get("transactionId").asText();

    MvcResult secondResult = mockMvc.perform(post("/api/v1/transactions/withdraw")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", idempotencyKey)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andReturn();

    String secondTransactionId = objectMapper.readTree(secondResult.getResponse().getContentAsString())
        .get("transactionId").asText();

    assertEquals(firstTransactionId, secondTransactionId);
  }

  @Test
  void shouldReturnSameTransactionForDuplicateTransferWithSameIdempotencyKey() throws Exception {
    DepositRequest depositRequest = new DepositRequest(new BigDecimal("500.00"), "USD");
    mockMvc.perform(post("/api/v1/transactions/deposit")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", "setup-deposit-for-transfer-idempotency-test")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(depositRequest)))
        .andExpect(status().isCreated());

    TransferRequest request = new TransferRequest(
        secondUserId,
        "USD",
        "USD",
        new BigDecimal("100.00"));
    String idempotencyKey = "transfer-idempotency-test-1";

    MvcResult firstResult = mockMvc.perform(post("/api/v1/transactions/transfer")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", idempotencyKey)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andReturn();

    String firstTransactionId = objectMapper.readTree(firstResult.getResponse().getContentAsString())
        .get("transactionId").asText();

    MvcResult secondResult = mockMvc.perform(post("/api/v1/transactions/transfer")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", idempotencyKey)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andReturn();

    String secondTransactionId = objectMapper.readTree(secondResult.getResponse().getContentAsString())
        .get("transactionId").asText();

    assertEquals(firstTransactionId, secondTransactionId);
  }

  @Test
  void shouldCreateDifferentTransactionsForDifferentIdempotencyKeys() throws Exception {
    DepositRequest request = new DepositRequest(new BigDecimal("100.00"), "USD");

    MvcResult firstResult = mockMvc.perform(post("/api/v1/transactions/deposit")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", "unique-key-1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andReturn();

    String firstTransactionId = objectMapper.readTree(firstResult.getResponse().getContentAsString())
        .get("transactionId").asText();

    MvcResult secondResult = mockMvc.perform(post("/api/v1/transactions/deposit")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", "unique-key-2")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.transactionId").exists())
        .andReturn();

    String secondTransactionId = objectMapper.readTree(secondResult.getResponse().getContentAsString())
        .get("transactionId").asText();

    assertNotEquals(firstTransactionId, secondTransactionId);
  }

  @Test
  void shouldFailDepositWhenIdempotencyKeyIsMissing() throws Exception {
    DepositRequest request = new DepositRequest(new BigDecimal("100.00"), "USD");

    mockMvc.perform(post("/api/v1/transactions/deposit")
        .header("Authorization", "Bearer " + userToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldFailWithdrawWhenIdempotencyKeyIsMissing() throws Exception {
    WithdrawRequest request = new WithdrawRequest(new BigDecimal("100.00"), "USD");

    mockMvc.perform(post("/api/v1/transactions/withdraw")
        .header("Authorization", "Bearer " + userToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldFailTransferWhenIdempotencyKeyIsMissing() throws Exception {
    TransferRequest request = new TransferRequest(
        secondUserId,
        "USD",
        "USD",
        new BigDecimal("100.00"));

    mockMvc.perform(post("/api/v1/transactions/transfer")
        .header("Authorization", "Bearer " + userToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldFailWhenIdempotencyKeyIsBlank() throws Exception {
    DepositRequest request = new DepositRequest(new BigDecimal("100.00"), "USD");

    mockMvc.perform(post("/api/v1/transactions/deposit")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", "   ")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldFailWhenIdempotencyKeyIsEmpty() throws Exception {
    WithdrawRequest request = new WithdrawRequest(new BigDecimal("100.00"), "USD");

    mockMvc.perform(post("/api/v1/transactions/withdraw")
        .header("Authorization", "Bearer " + userToken)
        .header("Idempotency-Key", "")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
