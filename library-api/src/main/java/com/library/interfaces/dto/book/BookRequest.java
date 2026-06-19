package com.library.interfaces.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookRequest(
        @NotBlank String isbn,
        @NotBlank String title,
        String publisher,
        Integer publishYear,
        String location,
        String summary,
        List<String> authorNames,
        List<String> categoryCodes
) {
}
