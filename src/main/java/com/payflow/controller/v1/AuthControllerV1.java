package com.payflow.controller.v1;

import com.payflow.dto.v1.response.AuthResponse;
import com.payflow.dto.v1.request.LoginRequest;
import com.payflow.dto.v1.request.RegisterRequest;
import com.payflow.dto.v1.response.UserResponse;
import com.payflow.entity.Role;
import com.payflow.entity.User;
import com.payflow.security.JwtTokenProvider;
import com.payflow.services.UserService;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication (v1)", description = "User authentication endpoints - Version 1")
public class AuthControllerV1 {

  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;

  public AuthControllerV1(UserService userService, JwtTokenProvider jwtTokenProvider) {
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
    Set<String> userRoles = user.getRoles()
        .stream().map(Role::getRole).collect(Collectors.toSet());
    String token = jwtTokenProvider.generateToken(user.getId(), userRoles);

    AuthResponse response = new AuthResponse(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        token,
        "Login successful");

    return ResponseEntity.ok(response);
  }

}
