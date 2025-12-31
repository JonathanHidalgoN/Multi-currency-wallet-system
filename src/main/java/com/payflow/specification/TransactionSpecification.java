package com.payflow.specification;

import com.payflow.DTOS.TransactionFilter;
import com.payflow.entity.Transaction;
import com.payflow.entity.Wallet;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

  public static Specification<Transaction> buildSpec(Wallet wallet, TransactionFilter filter) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      predicates.add(criteriaBuilder.equal(root.get(Transaction.Fields.WALLET), wallet));

      if (filter.currency() != null && !filter.currency().isBlank()) {
        predicates.add(criteriaBuilder.equal(root.get(Transaction.Fields.CURRENCY), filter.currency()));
      }

      if (filter.type() != null) {
        predicates.add(criteriaBuilder.equal(root.get(Transaction.Fields.TYPE), filter.type()));
      }

      if (filter.status() != null) {
        predicates.add(criteriaBuilder.equal(root.get(Transaction.Fields.STATUS), filter.status()));
      }

      if (filter.fromDate() != null) {
        LocalDateTime startOfDay = filter.fromDate().atStartOfDay();
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(Transaction.Fields.CREATED_AT), startOfDay));
      }

      if (filter.toDate() != null) {
        LocalDateTime endOfDay = filter.toDate().atTime(23, 59, 59);
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(Transaction.Fields.CREATED_AT), endOfDay));
      }

      if (filter.minAmount() != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(Transaction.Fields.AMOUNT), filter.minAmount()));
      }

      if (filter.maxAmount() != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(Transaction.Fields.AMOUNT), filter.maxAmount()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
