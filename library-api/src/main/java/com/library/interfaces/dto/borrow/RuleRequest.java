package com.library.interfaces.dto.borrow;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RuleRequest(
        @Min(1) int maxBorrowDays,
        @Min(1) int maxBorrowCount,
        @Min(0) int maxRenewCount,
        @NotNull @DecimalMin("0.00") BigDecimal finePerDay
) {
}
