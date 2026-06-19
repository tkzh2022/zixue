package com.library.infrastructure.persistence.jpa.borrow;

import com.library.domain.borrow.BorrowRecord;
import com.library.domain.borrow.BorrowRecordRepository;
import com.library.domain.borrow.BorrowStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class BorrowRecordRepositoryImpl implements BorrowRecordRepository {

    private final BorrowRecordJpaRepository repository;

    public BorrowRecordRepositoryImpl(BorrowRecordJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<BorrowRecord> findById(Long id) {
        return repository.findById(id).map(BorrowRecordMapper::toDomain);
    }

    @Override
    public Optional<BorrowRecord> findByIdForUpdate(Long id) {
        return repository.findByIdForUpdate(id).map(BorrowRecordMapper::toDomain);
    }

    @Override
    public List<BorrowRecord> findByReaderId(Long readerId) {
        return repository.findByReaderId(readerId).stream()
                .map(BorrowRecordMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowRecord> findByBookCopyId(Long bookCopyId) {
        return repository.findByBookCopyId(bookCopyId).stream()
                .map(BorrowRecordMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BorrowRecord> findActiveByBookCopyIdForUpdate(Long bookCopyId) {
        return repository.findActiveByBookCopyIdForUpdate(
                        bookCopyId, List.of(BorrowStatus.BORROWING, BorrowStatus.OVERDUE))
                .stream()
                .findFirst()
                .map(BorrowRecordMapper::toDomain);
    }

    @Override
    public List<BorrowRecord> findAll() {
        return repository.findAll().stream()
                .map(BorrowRecordMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public int countUnreturnedByReaderId(Long readerId) {
        return repository.countByReaderIdAndStatusIn(readerId, List.of(BorrowStatus.BORROWING, BorrowStatus.OVERDUE));
    }

    @Override
    public boolean hasOverdueRecords(Long readerId) {
        return repository.existsByReaderIdAndStatus(readerId, BorrowStatus.OVERDUE);
    }

    @Override
    public BorrowRecord save(BorrowRecord record) {
        BorrowRecordJpaEntity saved = repository.save(BorrowRecordMapper.toEntity(record));
        record.assignId(saved.getId());
        return BorrowRecordMapper.toDomain(saved);
    }
}
