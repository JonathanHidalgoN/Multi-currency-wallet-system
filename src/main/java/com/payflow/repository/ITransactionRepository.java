package com.payflow.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.payflow.entity.Transaction;
import com.payflow.entity.Wallet;

@Repository
public interface ITransactionRepository extends JpaRepository<Transaction, Long>,
                                                JpaSpecificationExecutor<Transaction> {
  Optional<Transaction> findByTransactionId(String transactionId);

  Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

  Page<Transaction> findByWallet(Wallet wallet, Pageable pageable);

  Page<Transaction> findByWalletAndType(
      Wallet wallet,
      Transaction.TransactionType type,
      Pageable page);

  Page<Transaction> findByWalletAndCurrency(
      Wallet wallet,
      String currency,
      Pageable page);

  Page<Transaction> findByWalletAndCreatedAtBetween(
      Wallet wallet,
      LocalDateTime startDate,
      LocalDateTime endDate,
      Pageable pageable);

  List<Transaction> findByRecipientUserId(Long recipientUserId);

}
