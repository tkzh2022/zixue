package com.library.domain.fine;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class FineRecord {

    private Long id;
    private Long borrowRecordId;
    private Long readerId;
    private BigDecimal amount;
    private String reason;
    private FineStatus status;
    private Instant createdAt;
    private Instant paidAt;

    private FineRecord() {
    }

    public static FineRecord create(Long borrowRecordId, Long readerId, BigDecimal amount, String reason, Instant now) {
        Objects.requireNonNull(borrowRecordId, "borrowRecordId");
        Objects.requireNonNull(readerId, "readerId");
        Objects.requireNonNull(amount, "amount");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        FineRecord record = new FineRecord();
        record.borrowRecordId = borrowRecordId;
        record.readerId = readerId;
        record.amount = amount;
        record.reason = reason != null ? reason : "Overdue fine";
        record.status = FineStatus.UNPAID;
        record.createdAt = now;
        return record;
    }

    public static FineRecord restore(Long id, Long borrowRecordId, Long readerId, BigDecimal amount, String reason,
                                     FineStatus status, Instant createdAt, Instant paidAt) {
        FineRecord record = new FineRecord();
        record.id = id;
        record.borrowRecordId = borrowRecordId;
        record.readerId = readerId;
        record.amount = amount;
        record.reason = reason;
        record.status = status;
        record.createdAt = createdAt;
        record.paidAt = paidAt;
        return record;
    }

    public void pay(BigDecimal paidAmount, Instant now) {
        if (this.status != FineStatus.UNPAID) {
            throw new IllegalStateException("Fine is not unpaid");
        }
        if (this.amount.compareTo(paidAmount) != 0) {
            throw new IllegalArgumentException("Paid amount does not match fine amount");
        }
        this.status = FineStatus.PAID;
        this.paidAt = now;
    }

    public void waive(Instant now) {
        if (this.status != FineStatus.UNPAID) {
            throw new IllegalStateException("Fine is not unpaid");
        }
        this.status = FineStatus.WAIVED;
        this.paidAt = now;
    }

    public Long id() { return id; }
    public Long borrowRecordId() { return borrowRecordId; }
    public Long readerId() { return readerId; }
    public BigDecimal amount() { return amount; }
    public String reason() { return reason; }
    public FineStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant paidAt() { return paidAt; }

    public void assignId(Long id) {
        this.id = id;
    }
}
