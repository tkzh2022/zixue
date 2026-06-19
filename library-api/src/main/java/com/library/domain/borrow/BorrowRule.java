package com.library.domain.borrow;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class BorrowRule {

    private Long id;
    private String readerType;
    private int maxBorrowDays;
    private int maxBorrowCount;
    private int maxRenewCount;
    private BigDecimal finePerDay;
    private Instant createdAt;
    private Instant updatedAt;

    private BorrowRule() {
    }

    public static BorrowRule create(String readerType, int maxBorrowDays, int maxBorrowCount, int maxRenewCount, BigDecimal finePerDay, Instant now) {
        Objects.requireNonNull(readerType, "readerType");
        Objects.requireNonNull(finePerDay, "finePerDay");
        if (maxBorrowDays <= 0 || maxBorrowCount <= 0 || maxRenewCount < 0 || finePerDay.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid rule parameters");
        }

        BorrowRule rule = new BorrowRule();
        rule.readerType = readerType;
        rule.maxBorrowDays = maxBorrowDays;
        rule.maxBorrowCount = maxBorrowCount;
        rule.maxRenewCount = maxRenewCount;
        rule.finePerDay = finePerDay;
        rule.createdAt = now;
        rule.updatedAt = now;
        return rule;
    }

    public static BorrowRule restore(Long id, String readerType, int maxBorrowDays, int maxBorrowCount, int maxRenewCount, BigDecimal finePerDay, Instant createdAt, Instant updatedAt) {
        BorrowRule rule = new BorrowRule();
        rule.id = id;
        rule.readerType = readerType;
        rule.maxBorrowDays = maxBorrowDays;
        rule.maxBorrowCount = maxBorrowCount;
        rule.maxRenewCount = maxRenewCount;
        rule.finePerDay = finePerDay;
        rule.createdAt = createdAt;
        rule.updatedAt = updatedAt;
        return rule;
    }

    public void updateInfo(int maxBorrowDays, int maxBorrowCount, int maxRenewCount, BigDecimal finePerDay, Instant now) {
        if (maxBorrowDays <= 0 || maxBorrowCount <= 0 || maxRenewCount < 0 || finePerDay.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid rule parameters");
        }
        this.maxBorrowDays = maxBorrowDays;
        this.maxBorrowCount = maxBorrowCount;
        this.maxRenewCount = maxRenewCount;
        this.finePerDay = finePerDay;
        this.updatedAt = now;
    }

    public Long id() { return id; }
    public String readerType() { return readerType; }
    public int maxBorrowDays() { return maxBorrowDays; }
    public int maxBorrowCount() { return maxBorrowCount; }
    public int maxRenewCount() { return maxRenewCount; }
    public BigDecimal finePerDay() { return finePerDay; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    public void assignId(Long id) {
        this.id = id;
    }
}
