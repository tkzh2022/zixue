package com.library.infrastructure.persistence.jpa.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface BookCopyJpaRepository extends JpaRepository<BookCopyJpaEntity, Long> {

    Optional<BookCopyJpaEntity> findByBarcode(String barcode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from BookCopyJpaEntity c where c.barcode = :barcode")
    Optional<BookCopyJpaEntity> findByBarcodeForUpdate(@Param("barcode") String barcode);

    List<BookCopyJpaEntity> findByBookIdAndDeletedAtIsNullOrderById(Long bookId);

    boolean existsByBarcode(String barcode);
}
