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
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;

  public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider) {
    this.userService = userService;
    this.jwtTokenProvider = jwtTokenProvider;
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
        user.getFullName());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @Valid @RequestBody LoginRequest request) {

    User user = userService.authenticate(request.email(), request.password());
    String token = jwtTokenProvider.generateToken(user.getId());

    AuthResponse response = new AuthResponse(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        token,
        "Login successful");

    return ResponseEntity.ok(response);
  }

}
