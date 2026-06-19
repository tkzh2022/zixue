package com.library.infrastructure.persistence.jpa.fine;

import com.library.domain.fine.FineRecord;
import com.library.domain.fine.FineRecordRepository;
import com.library.domain.fine.FineStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class FineRecordRepositoryImpl implements FineRecordRepository {

    private final FineRecordJpaRepository repository;

    public FineRecordRepositoryImpl(FineRecordJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<FineRecord> findById(Long id) {
        return repository.findById(id).map(FineRecordMapper::toDomain);
    }

    @Override
    public Optional<FineRecord> findByIdForUpdate(Long id) {
        return repository.findByIdForUpdate(id).map(FineRecordMapper::toDomain);
    }

    @Override
    public Optional<FineRecord> findByBorrowRecordId(Long borrowRecordId) {
        return repository.findByBorrowRecordId(borrowRecordId).map(FineRecordMapper::toDomain);
    }

    @Override
    public List<FineRecord> findByReaderId(Long readerId) {
        return repository.findByReaderId(readerId).stream()
                .map(FineRecordMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<FineRecord> findAll() {
        return repository.findAll().stream()
                .map(FineRecordMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasUnpaidFines(Long readerId) {
        return repository.existsByReaderIdAndStatus(readerId, FineStatus.UNPAID);
    }

    @Override
    public FineRecord save(FineRecord record) {
        FineRecordJpaEntity saved = repository.save(FineRecordMapper.toEntity(record));
        record.assignId(saved.getId());
        return FineRecordMapper.toDomain(saved);
    }
}
