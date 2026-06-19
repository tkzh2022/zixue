package com.library.application.book.command;

import java.util.List;

public record UpdateBookCommand(
        Long id,
        String title,
        String publisher,
        Integer publishYear,
        String location,
        String summary,
        List<String> authorNames,
        List<String> categoryCodes
) {
}
