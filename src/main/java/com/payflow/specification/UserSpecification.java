package com.payflow.specification;

import com.payflow.DTOS.UserFilter;
import com.payflow.entity.Role;
import com.payflow.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

  public static Specification<User> buildSpec(UserFilter filter) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (filter.email() != null && !filter.email().isBlank()) {
        predicates.add(criteriaBuilder.like(
            criteriaBuilder.lower(root.get(User.Fields.EMAIL)),
            "%" + filter.email().toLowerCase() + "%"));
      }

      if (filter.fullName() != null && !filter.fullName().isBlank()) {
        predicates.add(criteriaBuilder.like(
            criteriaBuilder.lower(root.get(User.Fields.FULL_NAME)),
            "%" + filter.fullName().toLowerCase() + "%"));
      }

      if (filter.enabled() != null) {
        predicates.add(criteriaBuilder.equal(root.get(User.Fields.ENABLED), filter.enabled()));
      }

      if (filter.fromDate() != null) {
        LocalDateTime startOfDay = filter.fromDate().atStartOfDay();
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(
            root.get(User.Fields.CREATED_AT), startOfDay));
      }

      if (filter.toDate() != null) {
        LocalDateTime endOfDay = filter.toDate().atTime(23, 59, 59);
        predicates.add(criteriaBuilder.lessThanOrEqualTo(
            root.get(User.Fields.CREATED_AT), endOfDay));
      }

      if (filter.roleName() != null && !filter.roleName().isBlank()) {
        Join<User, Role> rolesJoin = root.join(User.Fields.ROLES);
        predicates.add(criteriaBuilder.equal(rolesJoin.get(Role.Fields.ROLE), filter.roleName()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
