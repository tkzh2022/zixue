package com.library.interfaces.dto.book;

import com.library.domain.book.CopyStatus;

public record BookCopyResponse(Long id, Long bookId, String barcode, CopyStatus status) {
}
