package com.library.infrastructure.persistence.jpa.fine;

import com.library.domain.fine.FineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface FineRecordJpaRepository extends JpaRepository<FineRecordJpaEntity, Long> {
    
    Optional<FineRecordJpaEntity> findByBorrowRecordId(Long borrowRecordId);
    
    List<FineRecordJpaEntity> findByReaderId(Long readerId);
    
    boolean existsByReaderIdAndStatus(Long readerId, FineStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FineRecordJpaEntity f where f.id = :id")
    Optional<FineRecordJpaEntity> findByIdForUpdate(@Param("id") Long id);
}
