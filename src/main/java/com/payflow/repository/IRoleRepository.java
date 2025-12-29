package com.payflow.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payflow.entity.Role;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {

  Optional<Role> findByRole(String role);

}
