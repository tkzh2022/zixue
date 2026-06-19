package com.library.infrastructure.persistence.jpa.borrow;

import com.library.domain.borrow.BorrowRule;
import com.library.domain.borrow.BorrowRuleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class BorrowRuleRepositoryImpl implements BorrowRuleRepository {

    private final BorrowRuleJpaRepository repository;

    public BorrowRuleRepositoryImpl(BorrowRuleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<BorrowRule> findById(Long id) {
        return repository.findById(id).map(BorrowRuleMapper::toDomain);
    }

    @Override
    public Optional<BorrowRule> findByReaderType(String readerType) {
        return repository.findByReaderType(readerType).map(BorrowRuleMapper::toDomain);
    }

    @Override
    public List<BorrowRule> findAll() {
        return repository.findAll().stream()
                .map(BorrowRuleMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public BorrowRule save(BorrowRule rule) {
        BorrowRuleJpaEntity saved = repository.save(BorrowRuleMapper.toEntity(rule));
        rule.assignId(saved.getId());
        return BorrowRuleMapper.toDomain(saved);
    }
}
