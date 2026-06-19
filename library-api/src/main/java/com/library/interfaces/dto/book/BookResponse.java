package com.library.interfaces.dto.book;

import java.util.List;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String publisher,
        Integer publishYear,
        int totalCopies,
        int availableCopies,
        String location,
        String summary,
        List<String> authorNames,
        List<String> categoryCodes
) {
}
