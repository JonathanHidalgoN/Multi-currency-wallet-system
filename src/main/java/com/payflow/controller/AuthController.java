package com.payflow.controller;

import com.payflow.DTOS.AuthResponse;
import com.payflow.DTOS.LoginRequest;
import com.payflow.DTOS.RegisterRequest;
import com.payflow.DTOS.UserResponse;
import com.payflow.entity.User;
import com.payflow.security.JwtTokenProvider;
import com.payflow.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;
  private final PasswordEncoder passwordEncoder;

  public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider,
      PasswordEncoder passwordEncoder) {
    this.userService = userService;
    this.jwtTokenProvider = jwtTokenProvider;
    this.passwordEncoder = passwordEncoder;
  }

  @PostMapping("/register")
  public ResponseEntity<UserResponse> register(
      @Valid @RequestBody RegisterRequest request) {

    User user = userService.registerUser(
        request.email(),
        request.password(),
        request.fullName());

    UserResponse response = new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getFullName()
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @Valid @RequestBody LoginRequest request) {

    User user = userService.findByEmail(request.email())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new IllegalArgumentException("Invalid credentials");
    }

    String token = jwtTokenProvider.generateToken(user.getId());

    AuthResponse response = new AuthResponse(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        token,
        "Login successful"
    );

    return ResponseEntity.ok(response);
  }

}
