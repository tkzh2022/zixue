package com.library.domain.reader;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public class Reader {

    private Long id;
    private Long userAccountId;
    private String readerNo;
    private String name;
    private String phone;
    private String email;
    private ReaderStatus status;
    private LocalDate registerDate;
    private Instant deletedAt;

    private Reader() {
    }

    public static Reader create(Long userAccountId, String readerNo, String name, String phone, String email, LocalDate registerDate) {
        Objects.requireNonNull(userAccountId, "userAccountId");
        Objects.requireNonNull(readerNo, "readerNo");
        Objects.requireNonNull(name, "name");
        Reader reader = new Reader();
        reader.userAccountId = userAccountId;
        reader.readerNo = readerNo;
        reader.name = name;
        reader.phone = phone;
        reader.email = email;
        reader.status = ReaderStatus.ACTIVE;
        reader.registerDate = registerDate;
        return reader;
    }

    public static Reader restore(Long id, Long userAccountId, String readerNo, String name, String phone, String email,
                                 ReaderStatus status, LocalDate registerDate, Instant deletedAt) {
        Reader reader = new Reader();
        reader.id = id;
        reader.userAccountId = userAccountId;
        reader.readerNo = readerNo;
        reader.name = name;
        reader.phone = phone;
        reader.email = email;
        reader.status = status;
        reader.registerDate = registerDate;
        reader.deletedAt = deletedAt;
        return reader;
    }

    public void updateInfo(String name, String phone, String email) {
        this.name = Objects.requireNonNull(name, "name");
        this.phone = phone;
        this.email = email;
    }

    public void enable() {
        this.status = ReaderStatus.ACTIVE;
    }

    public void disable() {
        this.status = ReaderStatus.DISABLED;
    }

    public boolean isActive() {
        return this.status == ReaderStatus.ACTIVE && this.deletedAt == null;
    }

    public void delete(Instant now) {
        this.deletedAt = now;
    }

    public Long id() { return id; }
    public Long userAccountId() { return userAccountId; }
    public String readerNo() { return readerNo; }
    public String name() { return name; }
    public String phone() { return phone; }
    public String email() { return email; }
    public ReaderStatus status() { return status; }
    public LocalDate registerDate() { return registerDate; }
    public Instant deletedAt() { return deletedAt; }

    public void assignId(Long id) {
        this.id = id;
    }
}
