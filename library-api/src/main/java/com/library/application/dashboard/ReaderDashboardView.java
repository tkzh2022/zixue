package com.library.application.dashboard;

import java.math.BigDecimal;

public record ReaderDashboardView(
        long activeBorrows,
        long overdueBorrows,
        BigDecimal unpaidFines
) {
}
