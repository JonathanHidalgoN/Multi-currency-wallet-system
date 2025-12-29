package com.payflow.security;

import com.payflow.config.JwtProperties;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

  @Mock
  private JwtProperties jwtProperties;

  private JwtTokenProvider jwtTokenProvider;
  private String validSecret;
  private Long expiration;

  @BeforeEach
  void setUp() {
    validSecret = "this-is-a-very-secure-secret-key-for-testing-jwt-tokens-minimum-32-chars";
    expiration = 3600000L; // 1 hour

    when(jwtProperties.getSecret()).thenReturn(validSecret);
    when(jwtProperties.getExpiration()).thenReturn(expiration);

    jwtTokenProvider = new JwtTokenProvider(jwtProperties);
  }

  @Test
  void shouldGenerateValidToken() {
    Long userId = 123L;
    Set<String> roles = Set.of("USER");

    String token = jwtTokenProvider.generateToken(userId, roles);

    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.split("\\.").length == 3);
  }

  @Test
  void shouldGenerateDifferentTokensForDifferentUsers() {
    Long userId1 = 123L;
    Long userId2 = 456L;
    Set<String> roles = Set.of("USER");

    String token1 = jwtTokenProvider.generateToken(userId1, roles);
    String token2 = jwtTokenProvider.generateToken(userId2, roles);

    assertNotEquals(token1, token2);
  }

  @Test
  void shouldExtractUserIdFromToken() {
    Long expectedUserId = 789L;
    Set<String> roles = Set.of("USER");

    String token = jwtTokenProvider.generateToken(expectedUserId, roles);
    Long actualUserId = jwtTokenProvider.getUserIdFromToken(token);

    assertEquals(expectedUserId, actualUserId);
  }

  @Test
  void shouldValidateValidToken() {
    Long userId = 123L;
    Set<String> roles = Set.of("USER");
    String token = jwtTokenProvider.generateToken(userId, roles);

    boolean isValid = jwtTokenProvider.validateToken(token);

    assertTrue(isValid);
  }

  @Test
  void shouldRejectTokenWithInvalidSignature() {
    String tokenWithInvalidSignature = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjMiLCJpYXQiOjE2MTYyMzkwMjIsImV4cCI6MTYxNjI0MjYyMn0.invalid_signature";

    boolean isValid = jwtTokenProvider.validateToken(tokenWithInvalidSignature);

    assertFalse(isValid);
  }

  @Test
  void shouldRejectMalformedToken() {
    String malformedToken = "this.is.not.a.valid.jwt.token";

    boolean isValid = jwtTokenProvider.validateToken(malformedToken);

    assertFalse(isValid);
  }

  @Test
  void shouldRejectNullToken() {
    boolean isValid = jwtTokenProvider.validateToken(null);

    assertFalse(isValid);
  }

  @Test
  void shouldRejectEmptyToken() {
    boolean isValid = jwtTokenProvider.validateToken("");

    assertFalse(isValid);
  }

  @Test
  void shouldRejectExpiredToken() {
    Date now = new Date();
    Date expiredDate = new Date(now.getTime() - 1000);

    SecretKey key = Keys.hmacShaKeyFor(validSecret.getBytes(StandardCharsets.UTF_8));
    String expiredToken = Jwts.builder()
        .subject("123")
        .issuedAt(new Date(now.getTime() - 2000))
        .expiration(expiredDate)
        .signWith(key)
        .compact();

    boolean isValid = jwtTokenProvider.validateToken(expiredToken);

    assertFalse(isValid);
  }

  @Test
  void shouldRejectTokenWithDifferentSecret() {
    String differentSecret = "different-secret-key-for-testing-purposes-minimum-32-characters-long";
    SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + 3600000);

    String tokenWithDifferentSecret = Jwts.builder()
        .subject("123")
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(differentKey)
        .compact();

    boolean isValid = jwtTokenProvider.validateToken(tokenWithDifferentSecret);

    assertFalse(isValid);
  }

  @Test
  void shouldThrowExceptionWhenExtractingUserIdFromInvalidToken() {
    String invalidToken = "invalid.token.here";

    assertThrows(JwtException.class, () -> {
      jwtTokenProvider.getUserIdFromToken(invalidToken);
    });
  }

  @Test
  void shouldHandleTokenWithoutExpiration() {
    SecretKey key = Keys.hmacShaKeyFor(validSecret.getBytes(StandardCharsets.UTF_8));

    String tokenWithoutExpiration = Jwts.builder()
        .subject("123")
        .issuedAt(new Date())
        .signWith(key)
        .compact();

    boolean isValid = jwtTokenProvider.validateToken(tokenWithoutExpiration);

    assertTrue(isValid);
  }

  @Test
  void shouldExtractRolesFromToken() {
    Long userId = 123L;
    Set<String> expectedRoles = Set.of("USER", "ADMIN");

    String token = jwtTokenProvider.generateToken(userId, expectedRoles);
    Set<String> actualRoles = jwtTokenProvider.getRolesFromToken(token);

    assertEquals(expectedRoles, actualRoles);
  }

  @Test
  void shouldReturnEmptySetWhenTokenHasNoRoles() {
    Long userId = 123L;
    Set<String> emptyRoles = Set.of();

    String token = jwtTokenProvider.generateToken(userId, emptyRoles);
    Set<String> actualRoles = jwtTokenProvider.getRolesFromToken(token);

    assertTrue(actualRoles.isEmpty());
  }
}
