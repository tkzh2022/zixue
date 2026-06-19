package com.library.application.reader.command;

import java.time.LocalDate;

public record CreateReaderCommand(
        Long userAccountId,
        String readerNo,
        String name,
        String phone,
        String email,
        LocalDate registerDate
) {
}
