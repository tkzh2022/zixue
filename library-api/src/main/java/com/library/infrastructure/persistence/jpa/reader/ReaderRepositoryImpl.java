package com.library.infrastructure.persistence.jpa.reader;

import com.library.domain.reader.Reader;
import com.library.domain.reader.ReaderRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ReaderRepositoryImpl implements ReaderRepository {

    private final ReaderJpaRepository readerJpaRepository;

    public ReaderRepositoryImpl(ReaderJpaRepository readerJpaRepository) {
        this.readerJpaRepository = readerJpaRepository;
    }

    @Override
    public Optional<Reader> findById(Long id) {
        return readerJpaRepository.findById(id).map(ReaderMapper::toDomain);
    }

    @Override
    public Optional<Reader> findByReaderNo(String readerNo) {
        return readerJpaRepository.findByReaderNo(readerNo).map(ReaderMapper::toDomain);
    }

    @Override
    public Optional<Reader> findByUserAccountId(Long userAccountId) {
        return readerJpaRepository.findByUserAccountId(userAccountId).map(ReaderMapper::toDomain);
    }

    @Override
    public boolean existsByReaderNo(String readerNo) {
        return readerJpaRepository.existsByReaderNo(readerNo);
    }

    @Override
    public Reader save(Reader reader) {
        ReaderJpaEntity saved = readerJpaRepository.save(ReaderMapper.toEntity(reader));
        reader.assignId(saved.getId());
        return ReaderMapper.toDomain(saved);
    }

    @Override
    public java.util.List<Reader> findAll() {
        return readerJpaRepository.findAll().stream()
                .map(ReaderMapper::toDomain)
                .filter(reader -> reader.deletedAt() == null)
                .toList();
    }
}
