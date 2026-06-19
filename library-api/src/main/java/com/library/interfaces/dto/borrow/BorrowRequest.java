package com.library.interfaces.dto.borrow;

import jakarta.validation.constraints.NotBlank;

public record BorrowRequest(
        @NotBlank String readerNo,
        @NotBlank String barcode
) {
}
