package com.library.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountJpaEntity, Long> {

    Optional<UserAccountJpaEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
