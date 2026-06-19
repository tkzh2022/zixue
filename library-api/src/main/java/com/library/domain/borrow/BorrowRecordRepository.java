package com.library.domain.borrow;

import java.util.List;
import java.util.Optional;

public interface BorrowRecordRepository {

    Optional<BorrowRecord> findById(Long id);

    Optional<BorrowRecord> findByIdForUpdate(Long id);

    List<BorrowRecord> findByReaderId(Long readerId);

    List<BorrowRecord> findByBookCopyId(Long bookCopyId);

    Optional<BorrowRecord> findActiveByBookCopyIdForUpdate(Long bookCopyId);

    List<BorrowRecord> findAll();

    int countUnreturnedByReaderId(Long readerId);

    boolean hasOverdueRecords(Long readerId);

    BorrowRecord save(BorrowRecord record);
}
