package com.library.infrastructure.persistence.jpa.book;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorJpaRepository extends JpaRepository<AuthorJpaEntity, Long> {
    Optional<AuthorJpaEntity> findByName(String name);
}
