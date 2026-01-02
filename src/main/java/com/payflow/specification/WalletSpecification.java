package com.payflow.specification;

import com.payflow.DTOS.WalletFilter;
import com.payflow.entity.Wallet;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WalletSpecification {

  public static Specification<Wallet> buildSpec(WalletFilter filter) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (filter.userId() != null) {
        predicates.add(criteriaBuilder.equal(root.get(Wallet.Fields.USER).get("id"), filter.userId()));
      }

      if (filter.currency() != null && !filter.currency().isBlank()) {
        MapJoin<Wallet, String, BigDecimal> balancesJoin = root.joinMap(Wallet.Fields.BALANCES);
        predicates.add(criteriaBuilder.equal(balancesJoin.key(), filter.currency()));
      }

      if (filter.fromDate() != null) {
        LocalDateTime startOfDay = filter.fromDate().atStartOfDay();
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(
            root.get(Wallet.Fields.CREATED_AT), startOfDay));
      }

      if (filter.toDate() != null) {
        LocalDateTime endOfDay = filter.toDate().atTime(23, 59, 59);
        predicates.add(criteriaBuilder.lessThanOrEqualTo(
            root.get(Wallet.Fields.CREATED_AT), endOfDay));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
