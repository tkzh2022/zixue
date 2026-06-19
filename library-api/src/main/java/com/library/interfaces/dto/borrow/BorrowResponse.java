package com.library.interfaces.dto.borrow;

import com.library.domain.borrow.BorrowStatus;

import java.time.Instant;
import java.time.LocalDate;

public record BorrowResponse(
        Long id,
        Long readerId,
        Long bookCopyId,
        Instant borrowTime,
        LocalDate dueDate,
        Instant returnTime,
        int renewCount,
        BorrowStatus status
) {
}
