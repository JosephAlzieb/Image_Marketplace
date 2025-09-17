package com.marketplace.repository;

import com.marketplace.model.entity.User;
import com.marketplace.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Page<User> findByRoleIn(List<UserRole> roles, Pageable pageable);
}
