package com.library.interfaces.dto.dashboard;

import java.math.BigDecimal;

public record AdminDashboardResponse(
        long totalBooks,
        long totalReaders,
        long activeBorrows,
        BigDecimal unpaidFines
) {
}
