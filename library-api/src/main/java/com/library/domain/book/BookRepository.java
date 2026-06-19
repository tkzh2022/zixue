package com.library.domain.book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    Optional<Book> findById(Long id);

    Optional<Book> findByIdForUpdate(Long id);

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    Book save(Book book);

    Optional<BookCopy> findCopyByBarcode(String barcode);

    Optional<BookCopy> findCopyByBarcodeForUpdate(String barcode);

    Optional<BookCopy> findCopyById(Long id);

    List<BookCopy> findCopiesByBookId(Long bookId);

    BookCopy saveCopy(BookCopy copy);

    boolean existsCopyByBarcode(String barcode);

    List<Book> findAll();
}
