package com.library.domain.book;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Book {

    private Long id;
    private String isbn;
    private String title;
    private String publisher;
    private Integer publishYear;
    private int totalCopies;
    private int availableCopies;
    private String location;
    private String summary;
    private int version;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private List<String> authorNames = new ArrayList<>();
    private List<String> categoryCodes = new ArrayList<>();

    private Book() {
    }

    public static Book create(String isbn, String title, String publisher, Integer publishYear,
                              String location, String summary, List<String> authorNames, List<String> categoryCodes, Instant now) {
        Objects.requireNonNull(isbn, "isbn");
        Objects.requireNonNull(title, "title");
        Book book = new Book();
        book.isbn = isbn;
        book.title = title;
        book.publisher = publisher;
        book.publishYear = publishYear;
        book.totalCopies = 0;
        book.availableCopies = 0;
        book.location = location;
        book.summary = summary;
        book.version = 0;
        book.createdAt = now;
        book.updatedAt = now;
        if (authorNames != null) {
            book.authorNames.addAll(authorNames);
        }
        if (categoryCodes != null) {
            book.categoryCodes.addAll(categoryCodes);
        }
        return book;
    }

    public static Book restore(Long id, String isbn, String title, String publisher, Integer publishYear,
                               int totalCopies, int availableCopies, String location, String summary,
                               int version, Instant createdAt, Instant updatedAt, Instant deletedAt,
                               List<String> authorNames, List<String> categoryCodes) {
        Book book = new Book();
        book.id = id;
        book.isbn = isbn;
        book.title = title;
        book.publisher = publisher;
        book.publishYear = publishYear;
        book.totalCopies = totalCopies;
        book.availableCopies = availableCopies;
        book.location = location;
        book.summary = summary;
        book.version = version;
        book.createdAt = createdAt;
        book.updatedAt = updatedAt;
        book.deletedAt = deletedAt;
        if (authorNames != null) book.authorNames.addAll(authorNames);
        if (categoryCodes != null) book.categoryCodes.addAll(categoryCodes);
        return book;
    }

    public void updateInfo(String title, String publisher, Integer publishYear,
                           String location, String summary, List<String> authorNames, List<String> categoryCodes, Instant now) {
        this.title = Objects.requireNonNull(title, "title");
        this.publisher = publisher;
        this.publishYear = publishYear;
        this.location = location;
        this.summary = summary;
        this.authorNames.clear();
        if (authorNames != null) this.authorNames.addAll(authorNames);
        this.categoryCodes.clear();
        if (categoryCodes != null) this.categoryCodes.addAll(categoryCodes);
        this.updatedAt = now;
    }

    public void addCopy() {
        this.totalCopies++;
        this.availableCopies++;
    }

    public void removeCopy(boolean wasAvailable) {
        if (this.totalCopies <= 0) {
            throw new IllegalStateException("Total copies cannot be negative");
        }
        this.totalCopies--;
        if (wasAvailable) {
            this.availableCopies--;
        }
    }

    public void borrowCopy() {
        if (this.availableCopies <= 0) {
            throw new IllegalStateException("No available copies to borrow");
        }
        this.availableCopies--;
    }

    public void returnCopy() {
        if (this.availableCopies >= this.totalCopies) {
            throw new IllegalStateException("Available copies cannot exceed total copies");
        }
        this.availableCopies++;
    }

    public void delete(Instant now) {
        if (this.totalCopies > this.availableCopies) {
            throw new IllegalStateException("Cannot delete book with unreturned copies");
        }
        this.deletedAt = now;
    }

    public Long id() { return id; }
    public String isbn() { return isbn; }
    public String title() { return title; }
    public String publisher() { return publisher; }
    public Integer publishYear() { return publishYear; }
    public int totalCopies() { return totalCopies; }
    public int availableCopies() { return availableCopies; }
    public String location() { return location; }
    public String summary() { return summary; }
    public int version() { return version; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public Instant deletedAt() { return deletedAt; }
    public List<String> authorNames() { return Collections.unmodifiableList(authorNames); }
    public List<String> categoryCodes() { return Collections.unmodifiableList(categoryCodes); }

    public void assignId(Long id) {
        this.id = id;
    }
}
