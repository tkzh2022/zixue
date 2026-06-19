package com.library.infrastructure.persistence.jpa.book;

import com.library.domain.book.Book;
import com.library.domain.book.BookCopy;
import com.library.domain.book.BookRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class BookRepositoryImpl implements BookRepository {

    private final BookJpaRepository bookJpaRepository;
    private final BookCopyJpaRepository bookCopyJpaRepository;
    private final AuthorJpaRepository authorJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;

    public BookRepositoryImpl(BookJpaRepository bookJpaRepository,
                              BookCopyJpaRepository bookCopyJpaRepository,
                              AuthorJpaRepository authorJpaRepository,
                              CategoryJpaRepository categoryJpaRepository) {
        this.bookJpaRepository = bookJpaRepository;
        this.bookCopyJpaRepository = bookCopyJpaRepository;
        this.authorJpaRepository = authorJpaRepository;
        this.categoryJpaRepository = categoryJpaRepository;
    }

    @Override
    public Optional<Book> findById(Long id) {
        return bookJpaRepository.findByIdWithAssociations(id)
                .filter(entity -> entity.getDeletedAt() == null)
                .map(BookMapper::toDomain);
    }

    @Override
    public Optional<Book> findByIdForUpdate(Long id) {
        return bookJpaRepository.findByIdForUpdate(id).map(BookMapper::toDomain);
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return bookJpaRepository.findByIsbn(isbn)
                .filter(entity -> entity.getDeletedAt() == null)
                .map(BookMapper::toDomain);
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return bookJpaRepository.existsByIsbn(isbn);
    }

    @Override
    public Book save(Book book) {
        BookJpaEntity entity = BookMapper.toEntity(book);

        // Map authors
        for (String authorName : book.authorNames()) {
            AuthorJpaEntity authorEntity = authorJpaRepository.findByName(authorName)
                    .orElseGet(() -> {
                        AuthorJpaEntity a = new AuthorJpaEntity();
                        a.setName(authorName);
                        return authorJpaRepository.save(a);
                    });
            entity.getAuthors().add(authorEntity);
        }

        // Map categories
        for (String categoryCode : book.categoryCodes()) {
            CategoryJpaEntity categoryEntity = categoryJpaRepository.findByCode(categoryCode)
                    .orElseGet(() -> {
                        CategoryJpaEntity c = new CategoryJpaEntity();
                        c.setCode(categoryCode);
                        c.setName(categoryCode); // default name to code
                        return categoryJpaRepository.save(c);
                    });
            entity.getCategories().add(categoryEntity);
        }

        BookJpaEntity saved = bookJpaRepository.save(entity);
        book.assignId(saved.getId());
        return BookMapper.toDomain(saved);
    }

    @Override
    public Optional<BookCopy> findCopyByBarcode(String barcode) {
        return bookCopyJpaRepository.findByBarcode(barcode)
                .filter(entity -> entity.getDeletedAt() == null)
                .map(BookMapper::toDomain);
    }

    @Override
    public Optional<BookCopy> findCopyByBarcodeForUpdate(String barcode) {
        return bookCopyJpaRepository.findByBarcodeForUpdate(barcode).map(BookMapper::toDomain);
    }

    @Override
    public Optional<BookCopy> findCopyById(Long id) {
        return bookCopyJpaRepository.findById(id)
                .filter(entity -> entity.getDeletedAt() == null)
                .map(BookMapper::toDomain);
    }

    @Override
    public java.util.List<BookCopy> findCopiesByBookId(Long bookId) {
        return bookCopyJpaRepository.findByBookIdAndDeletedAtIsNullOrderById(bookId).stream()
                .map(BookMapper::toDomain)
                .toList();
    }

    @Override
    public BookCopy saveCopy(BookCopy copy) {
        BookCopyJpaEntity saved = bookCopyJpaRepository.save(BookMapper.toEntity(copy));
        copy.assignId(saved.getId());
        return BookMapper.toDomain(saved);
    }

    @Override
    public boolean existsCopyByBarcode(String barcode) {
        return bookCopyJpaRepository.existsByBarcode(barcode);
    }

    @Override
    public java.util.List<Book> findAll() {
        return bookJpaRepository.findAllWithAssociations().stream()
                .map(BookMapper::toDomain)
                .toList();
    }
}
