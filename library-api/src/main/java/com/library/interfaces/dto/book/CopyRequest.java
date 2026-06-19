package com.library.interfaces.dto.book;

import jakarta.validation.constraints.NotBlank;

public record CopyRequest(
        @NotBlank String barcode
) {
}
