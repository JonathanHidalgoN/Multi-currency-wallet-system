package com.payflow.controller;

import com.payflow.DTOS.FullWalletResponse;
import com.payflow.DTOS.UserDTO;
import com.payflow.DTOS.UserFilter;
import com.payflow.DTOS.WalletFilter;
import com.payflow.entity.Role;
import com.payflow.entity.User;
import com.payflow.entity.Wallet;
import com.payflow.services.UserService;
import com.payflow.services.WalletService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Validated
public class AdminController {

  private final UserService userService;
  private final WalletService walletService;

  public AdminController(UserService userService, WalletService walletService) {
    this.userService = userService;
    this.walletService = walletService;
  }

  @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
  @GetMapping("/users")
  public ResponseEntity<Page<UserDTO>> getAllUsers(
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
      @Valid @ModelAttribute UserFilter filter) {

    Page<User> userDTOs = userService.getUsers(filter, pageable);
    Page<UserDTO> dtoPage = userDTOs.map(u -> new UserDTO(
        u.getId(),
        u.getEmail(),
        u.getFullName(),
        u.getEnabled(),
        u.getCreatedAt(),
        u.getUpdatedAt(),
        u.getRoles().stream().map(Role::getRole).collect(Collectors.toSet())));

    return ResponseEntity.ok(dtoPage);
  }

  @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
  @GetMapping("/wallets")
  public ResponseEntity<Page<FullWalletResponse>> getAllWallets(
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
      @Valid @ModelAttribute WalletFilter filter) {

    Page<Wallet> wallets = walletService.getWallets(filter, pageable);
    Page<FullWalletResponse> dtoPage = wallets.map(w -> new FullWalletResponse(
        w.getId(),
        w.getUser().getId(),
        w.getBalances(),
        w.getCreatedAt(),
        w.getUpdatedAt()));

    return ResponseEntity.ok(dtoPage);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/users/{userId}/disable")
  public ResponseEntity<String> disableUser(@PathVariable("userId") @Positive long userId) {
    userService.disableUser(userId);
    return ResponseEntity.ok("User disabled succesfully");
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/users/{userId}/enable")
  public ResponseEntity<String> enableUser(@PathVariable("userId") @Positive long userId) {
    userService.enableUser(userId);
    return ResponseEntity.ok("User enabled successfully");
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/users/{userId}/roles")
  public ResponseEntity<String> updateUserRoles(
      @PathVariable("userId") @Positive long userId,
      @RequestBody @NotEmpty Set<String> roleNames) {
    userService.updateUserRoles(userId, roleNames);
    return ResponseEntity.ok("Roles updated successfully");
  }

}
