package com.library.interfaces.dto.borrow;

import java.math.BigDecimal;

public record RuleResponse(
        Long id,
        String readerType,
        int maxBorrowDays,
        int maxBorrowCount,
        int maxRenewCount,
        BigDecimal finePerDay
) {
}
