package com.library.domain.reader;

import java.util.Optional;
import java.util.List;

public interface ReaderRepository {

    Optional<Reader> findById(Long id);

    Optional<Reader> findByReaderNo(String readerNo);

    Optional<Reader> findByUserAccountId(Long userAccountId);

    boolean existsByReaderNo(String readerNo);

    Reader save(Reader reader);

    List<Reader> findAll();
}
