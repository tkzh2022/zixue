package com.library.interfaces.dto.fine;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PayFineRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount
) {
}
