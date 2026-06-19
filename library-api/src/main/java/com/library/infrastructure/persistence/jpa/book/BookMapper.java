package com.library.infrastructure.persistence.jpa.book;

import com.library.domain.book.Book;
import com.library.domain.book.BookCopy;

import java.util.List;
import java.util.stream.Collectors;

final class BookMapper {

    private BookMapper() {
    }

    static Book toDomain(BookJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        List<String> authorNames = entity.getAuthors().stream()
                .map(AuthorJpaEntity::getName)
                .collect(Collectors.toList());
        List<String> categoryCodes = entity.getCategories().stream()
                .map(CategoryJpaEntity::getCode)
                .collect(Collectors.toList());

        return Book.restore(
                entity.getId(),
                entity.getIsbn(),
                entity.getTitle(),
                entity.getPublisher(),
                entity.getPublishYear(),
                entity.getTotalCopies(),
                entity.getAvailableCopies(),
                entity.getLocation(),
                entity.getSummary(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                authorNames,
                categoryCodes
        );
    }

    static BookJpaEntity toEntity(Book domain) {
        if (domain == null) {
            return null;
        }
        BookJpaEntity entity = new BookJpaEntity();
        entity.setId(domain.id());
        entity.setIsbn(domain.isbn());
        entity.setTitle(domain.title());
        entity.setPublisher(domain.publisher());
        entity.setPublishYear(domain.publishYear());
        entity.setTotalCopies(domain.totalCopies());
        entity.setAvailableCopies(domain.availableCopies());
        entity.setLocation(domain.location());
        entity.setSummary(domain.summary());
        entity.setVersion(domain.version());
        entity.setCreatedAt(domain.createdAt());
        entity.setUpdatedAt(domain.updatedAt());
        entity.setDeletedAt(domain.deletedAt());
        return entity;
    }

    static BookCopy toDomain(BookCopyJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return BookCopy.restore(
                entity.getId(),
                entity.getBookId(),
                entity.getBarcode(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getDeletedAt()
        );
    }

    static BookCopyJpaEntity toEntity(BookCopy domain) {
        if (domain == null) {
            return null;
        }
        BookCopyJpaEntity entity = new BookCopyJpaEntity();
        entity.setId(domain.id());
        entity.setBookId(domain.bookId());
        entity.setBarcode(domain.barcode());
        entity.setStatus(domain.status());
        entity.setCreatedAt(domain.createdAt());
        entity.setDeletedAt(domain.deletedAt());
        return entity;
    }
}
