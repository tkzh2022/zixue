package com.library.interfaces.dto.reader;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReaderRequest(
        @NotNull Long userAccountId,
        @NotBlank String readerNo,
        @NotBlank String name,
        String phone,
        String email,
        @NotNull LocalDate registerDate
) {
}
