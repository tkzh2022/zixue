package com.library.infrastructure.persistence.jpa.reader;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReaderJpaRepository extends JpaRepository<ReaderJpaEntity, Long> {

    Optional<ReaderJpaEntity> findByReaderNo(String readerNo);

    Optional<ReaderJpaEntity> findByUserAccountId(Long userAccountId);

    boolean existsByReaderNo(String readerNo);
}
