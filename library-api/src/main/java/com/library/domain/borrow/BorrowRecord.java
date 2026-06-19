package com.library.domain.borrow;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

public class BorrowRecord {

    private Long id;
    private Long readerId;
    private Long bookCopyId;
    private Instant borrowTime;
    private LocalDate dueDate;
    private Instant returnTime;
    private int renewCount;
    private BorrowStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private BorrowRecord() {
    }

    public static BorrowRecord create(Long readerId, Long bookCopyId, int maxBorrowDays, Instant now) {
        Objects.requireNonNull(readerId, "readerId");
        Objects.requireNonNull(bookCopyId, "bookCopyId");
        if (maxBorrowDays <= 0) {
            throw new IllegalArgumentException("maxBorrowDays must be positive");
        }

        BorrowRecord record = new BorrowRecord();
        record.readerId = readerId;
        record.bookCopyId = bookCopyId;
        record.borrowTime = now;
        record.dueDate = LocalDate.ofInstant(now, ZoneId.systemDefault()).plusDays(maxBorrowDays);
        record.renewCount = 0;
        record.status = BorrowStatus.BORROWING;
        record.createdAt = now;
        record.updatedAt = now;
        return record;
    }

    public static BorrowRecord restore(Long id, Long readerId, Long bookCopyId, Instant borrowTime, LocalDate dueDate,
                                       Instant returnTime, int renewCount, BorrowStatus status, Instant createdAt, Instant updatedAt) {
        BorrowRecord record = new BorrowRecord();
        record.id = id;
        record.readerId = readerId;
        record.bookCopyId = bookCopyId;
        record.borrowTime = borrowTime;
        record.dueDate = dueDate;
        record.returnTime = returnTime;
        record.renewCount = renewCount;
        record.status = status;
        record.createdAt = createdAt;
        record.updatedAt = updatedAt;
        return record;
    }

    public void returnBook(Instant now) {
        if (this.status == BorrowStatus.RETURNED || this.status == BorrowStatus.LOST) {
            throw new IllegalStateException("Book is already returned or lost");
        }
        this.status = BorrowStatus.RETURNED;
        this.returnTime = now;
        this.updatedAt = now;
    }

    public void renew(int maxRenewCount, int renewDays, Instant now) {
        if (this.status != BorrowStatus.BORROWING) {
            throw new IllegalStateException("Only borrowing records can be renewed");
        }
        if (this.renewCount >= maxRenewCount) {
            throw new IllegalStateException("Max renew count reached");
        }
        this.renewCount++;
        this.dueDate = this.dueDate.plusDays(renewDays);
        this.updatedAt = now;
    }

    public void markOverdue(Instant now) {
        if (this.status == BorrowStatus.BORROWING) {
            this.status = BorrowStatus.OVERDUE;
            this.updatedAt = now;
        }
    }

    public void markLost(Instant now) {
        if (this.status == BorrowStatus.BORROWING || this.status == BorrowStatus.OVERDUE) {
            this.status = BorrowStatus.LOST;
            this.updatedAt = now;
        }
    }

    public boolean isOverdue(LocalDate currentDate) {
        return (this.status == BorrowStatus.BORROWING || this.status == BorrowStatus.OVERDUE) 
                && currentDate.isAfter(this.dueDate);
    }

    public Long id() { return id; }
    public Long readerId() { return readerId; }
    public Long bookCopyId() { return bookCopyId; }
    public Instant borrowTime() { return borrowTime; }
    public LocalDate dueDate() { return dueDate; }
    public Instant returnTime() { return returnTime; }
    public int renewCount() { return renewCount; }
    public BorrowStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    public void assignId(Long id) {
        this.id = id;
    }
}
