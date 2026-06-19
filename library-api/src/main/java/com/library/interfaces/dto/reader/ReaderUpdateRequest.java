package com.library.interfaces.dto.reader;

import jakarta.validation.constraints.NotBlank;

public record ReaderUpdateRequest(@NotBlank String name, String phone, String email) {
}
