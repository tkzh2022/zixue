package com.library.domain.fine;

import java.util.List;
import java.util.Optional;

public interface FineRecordRepository {

    Optional<FineRecord> findById(Long id);

    Optional<FineRecord> findByIdForUpdate(Long id);

    Optional<FineRecord> findByBorrowRecordId(Long borrowRecordId);

    List<FineRecord> findByReaderId(Long readerId);

    List<FineRecord> findAll();

    boolean hasUnpaidFines(Long readerId);

    FineRecord save(FineRecord record);
}
