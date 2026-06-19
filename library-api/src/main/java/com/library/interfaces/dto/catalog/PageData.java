package com.library.interfaces.dto.catalog;

import com.library.interfaces.dto.book.BookResponse;

import java.util.List;

public record PageData<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
    public static <T> PageData<T> from(List<T> values, int requestedPage, int requestedSize) {
        int page = Math.max(1, requestedPage);
        int size = Math.min(100, Math.max(1, requestedSize));
        int from = Math.min((page - 1) * size, values.size());
        int to = Math.min(from + size, values.size());
        int totalPages = values.isEmpty() ? 0 : (int) Math.ceil((double) values.size() / size);
        return new PageData<>(values.subList(from, to), values.size(), totalPages, page, size);
    }
}
