package com.payflow.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.payflow.entity.User;
import com.payflow.entity.Wallet;

@Repository
public interface IWalletRepository extends JpaRepository<Wallet, Long>, JpaSpecificationExecutor<Wallet> {
  @Query("SELECT w FROM Wallet w WHERE w.user = :user")
  Optional<Wallet> findByUserWithoutLock(@Param("user") User user);

  @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId")
  Optional<Wallet> findByUserIdWithoutLock(@Param("userId") Long userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT w FROM Wallet w WHERE w.id = :id")
  Optional<Wallet> findByIdWithLock(@Param("id") Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId")
  Optional<Wallet> findByUserIdWithLock(@Param("userId") Long userId);
}
