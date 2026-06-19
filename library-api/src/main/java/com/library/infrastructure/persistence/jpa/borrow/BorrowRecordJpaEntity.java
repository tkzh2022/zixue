package com.library.infrastructure.persistence.jpa.borrow;

import com.library.domain.borrow.BorrowStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "borrow_record", indexes = {
        @Index(name = "idx_borrow_reader", columnList = "reader_id"),
        @Index(name = "idx_borrow_copy", columnList = "book_copy_id"),
        @Index(name = "idx_borrow_status", columnList = "status")
})
public class BorrowRecordJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "reader_id", nullable = false)
    private Long readerId;

    @Column(name = "book_copy_id", nullable = false)
    private Long bookCopyId;

    @Column(name = "borrow_time", nullable = false)
    private Instant borrowTime;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_time")
    private Instant returnTime;

    @Column(name = "renew_count", nullable = false)
    private int renewCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16, columnDefinition = "varchar(16)")
    private BorrowStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReaderId() {
        return readerId;
    }

    public void setReaderId(Long readerId) {
        this.readerId = readerId;
    }

    public Long getBookCopyId() {
        return bookCopyId;
    }

    public void setBookCopyId(Long bookCopyId) {
        this.bookCopyId = bookCopyId;
    }

    public Instant getBorrowTime() {
        return borrowTime;
    }

    public void setBorrowTime(Instant borrowTime) {
        this.borrowTime = borrowTime;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Instant getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(Instant returnTime) {
        this.returnTime = returnTime;
    }

    public int getRenewCount() {
        return renewCount;
    }

    public void setRenewCount(int renewCount) {
        this.renewCount = renewCount;
    }

    public BorrowStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
