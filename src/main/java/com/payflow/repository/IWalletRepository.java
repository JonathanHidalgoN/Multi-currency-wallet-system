package com.payflow.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payflow.entity.User;
import com.payflow.entity.Wallet;

@Repository
public interface IWalletRepository extends JpaRepository<Wallet, Long> {
  Optional<Wallet> findByUser(User user);

  Optional<Wallet> findByUserId(Long userId);
}
