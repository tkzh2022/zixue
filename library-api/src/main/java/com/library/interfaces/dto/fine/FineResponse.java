package com.library.interfaces.dto.fine;

import com.library.domain.fine.FineStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record FineResponse(
        Long id,
        Long borrowRecordId,
        Long readerId,
        BigDecimal amount,
        String reason,
        FineStatus status,
        Instant createdAt,
        Instant paidAt
) {
}
