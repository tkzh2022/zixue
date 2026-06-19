package com.library.interfaces.dto.reader;

import jakarta.validation.constraints.NotBlank;

public record ReaderStatusRequest(
        @NotBlank String status
) {
}
