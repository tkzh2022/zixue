package com.library.application.dashboard;

import java.math.BigDecimal;

public record AdminDashboardView(
        long totalBooks,
        long totalReaders,
        long activeBorrows,
        BigDecimal unpaidFines
) {
}
