package com.library.infrastructure.persistence.jpa.borrow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BorrowRuleJpaRepository extends JpaRepository<BorrowRuleJpaEntity, Long> {
    Optional<BorrowRuleJpaEntity> findByReaderType(String readerType);
}
