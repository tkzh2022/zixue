package com.library.interfaces.dto.dashboard;

import java.math.BigDecimal;

public record ReaderDashboardResponse(
        long activeBorrows,
        long overdueBorrows,
        BigDecimal unpaidFines
) {
}
