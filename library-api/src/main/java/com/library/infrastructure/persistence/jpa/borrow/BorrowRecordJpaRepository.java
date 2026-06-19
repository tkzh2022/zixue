package com.library.infrastructure.persistence.jpa.borrow;

import com.library.domain.borrow.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface BorrowRecordJpaRepository extends JpaRepository<BorrowRecordJpaEntity, Long> {
    
    List<BorrowRecordJpaEntity> findByReaderId(Long readerId);
    
    List<BorrowRecordJpaEntity> findByBookCopyId(Long bookCopyId);
    
    int countByReaderIdAndStatusIn(Long readerId, List<BorrowStatus> statuses);
    
    boolean existsByReaderIdAndStatus(Long readerId, BorrowStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from BorrowRecordJpaEntity r where r.id = :id")
    Optional<BorrowRecordJpaEntity> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from BorrowRecordJpaEntity r where r.bookCopyId = :bookCopyId and r.status in :statuses order by r.id desc")
    List<BorrowRecordJpaEntity> findActiveByBookCopyIdForUpdate(
            @Param("bookCopyId") Long bookCopyId,
            @Param("statuses") List<BorrowStatus> statuses);
}
