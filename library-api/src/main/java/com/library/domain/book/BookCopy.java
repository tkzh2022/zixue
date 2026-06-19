package com.library.domain.book;

import java.time.Instant;
import java.util.Objects;

public class BookCopy {

    private Long id;
    private Long bookId;
    private String barcode;
    private CopyStatus status;
    private Instant createdAt;
    private Instant deletedAt;

    private BookCopy() {
    }

    public static BookCopy create(Long bookId, String barcode, Instant now) {
        Objects.requireNonNull(bookId, "bookId");
        Objects.requireNonNull(barcode, "barcode");
        BookCopy copy = new BookCopy();
        copy.bookId = bookId;
        copy.barcode = barcode;
        copy.status = CopyStatus.IN_LIBRARY;
        copy.createdAt = now;
        return copy;
    }

    public static BookCopy restore(Long id, Long bookId, String barcode, CopyStatus status, Instant createdAt, Instant deletedAt) {
        BookCopy copy = new BookCopy();
        copy.id = id;
        copy.bookId = bookId;
        copy.barcode = barcode;
        copy.status = status;
        copy.createdAt = createdAt;
        copy.deletedAt = deletedAt;
        return copy;
    }

    public boolean isAvailable() {
        return status == CopyStatus.IN_LIBRARY && deletedAt == null;
    }

    public void markBorrowed() {
        if (!isAvailable()) {
            throw new IllegalStateException("Copy is not available for borrowing");
        }
        this.status = CopyStatus.BORROWED;
    }

    public void markReturned() {
        if (this.status != CopyStatus.BORROWED) {
            throw new IllegalStateException("Copy is not borrowed");
        }
        this.status = CopyStatus.IN_LIBRARY;
    }

    public void markLost(Instant now) {
        this.status = CopyStatus.LOST;
        // Depending on business rules, we might also set deletedAt = now
    }

    public void markMaintenance() {
        this.status = CopyStatus.MAINTENANCE;
    }

    public void delete(Instant now) {
        if (this.status == CopyStatus.BORROWED) {
            throw new IllegalStateException("Cannot delete a borrowed copy");
        }
        this.deletedAt = now;
    }

    public Long id() { return id; }
    public Long bookId() { return bookId; }
    public String barcode() { return barcode; }
    public CopyStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant deletedAt() { return deletedAt; }

    public void assignId(Long id) {
        this.id = id;
    }
}
