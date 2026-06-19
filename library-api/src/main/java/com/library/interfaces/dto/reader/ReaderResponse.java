package com.library.interfaces.dto.reader;

import com.library.domain.reader.ReaderStatus;

import java.time.LocalDate;

public record ReaderResponse(
        Long id,
        Long userAccountId,
        String readerNo,
        String name,
        String phone,
        String email,
        ReaderStatus status,
        LocalDate registerDate
) {
}
