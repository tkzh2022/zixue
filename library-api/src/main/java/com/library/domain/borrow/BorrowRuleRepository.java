package com.library.domain.borrow;

import java.util.List;
import java.util.Optional;

public interface BorrowRuleRepository {

    Optional<BorrowRule> findById(Long id);

    Optional<BorrowRule> findByReaderType(String readerType);

    List<BorrowRule> findAll();

    BorrowRule save(BorrowRule rule);
}
