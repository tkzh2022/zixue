package com.library.application.book.command;

import java.util.List;

public record CreateBookCommand(
        String isbn,
        String title,
        String publisher,
        Integer publishYear,
        String location,
        String summary,
        List<String> authorNames,
        List<String> categoryCodes
) {
}
