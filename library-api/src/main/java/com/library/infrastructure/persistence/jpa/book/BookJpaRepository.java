package com.library.infrastructure.persistence.jpa.book;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface BookJpaRepository extends JpaRepository<BookJpaEntity, Long> {

    Optional<BookJpaEntity> findByIsbn(String isbn);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BookJpaEntity b where b.id = :id")
    Optional<BookJpaEntity> findByIdForUpdate(@Param("id") Long id);

    boolean existsByIsbn(String isbn);

    @EntityGraph(attributePaths = {"authors", "categories"})
    @Query("select distinct b from BookJpaEntity b where b.deletedAt is null")
    List<BookJpaEntity> findAllWithAssociations();

    @EntityGraph(attributePaths = {"authors", "categories"})
    @Query("select b from BookJpaEntity b where b.id = :id")
    Optional<BookJpaEntity> findByIdWithAssociations(@Param("id") Long id);
}
