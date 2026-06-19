package com.library.application.book;

import com.library.application.book.command.CreateBookCommand;
import com.library.application.book.command.UpdateBookCommand;
import com.library.domain.book.Book;
import com.library.domain.book.BookCopy;
import com.library.domain.book.BookRepository;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class BookApplicationService {

    private final BookRepository bookRepository;

    public BookApplicationService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public Long createBook(CreateBookCommand cmd) {
        if (bookRepository.existsByIsbn(cmd.isbn())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "ISBN already exists");
        }
        Book book = Book.create(
                cmd.isbn(),
                cmd.title(),
                cmd.publisher(),
                cmd.publishYear(),
                cmd.location(),
                cmd.summary(),
                cmd.authorNames(),
                cmd.categoryCodes(),
                Instant.now()
        );
        return bookRepository.save(book).id();
    }

    @Transactional
    public void updateBook(UpdateBookCommand cmd) {
        Book book = bookRepository.findById(cmd.id())
                .orElseThrow(() -> new BusinessException(ResultCode.PARAM_INVALID, "Book not found"));
        book.updateInfo(
                cmd.title(),
                cmd.publisher(),
                cmd.publishYear(),
                cmd.location(),
                cmd.summary(),
                cmd.authorNames(),
                cmd.categoryCodes(),
                Instant.now()
        );
        bookRepository.save(book);
    }

    @Transactional
    public void addCopy(Long bookId, String barcode) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ResultCode.PARAM_INVALID, "Book not found"));

        if (bookRepository.existsCopyByBarcode(barcode)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "Barcode already exists");
        }

        BookCopy copy = BookCopy.create(bookId, barcode, Instant.now());
        book.addCopy();

        bookRepository.saveCopy(copy);
        bookRepository.save(book);
    }

    @Transactional
    public void deleteCopy(Long copyId) {
        BookCopy copy = bookRepository.findCopyById(copyId)
                .orElseThrow(() -> new BusinessException(ResultCode.PARAM_INVALID, "Copy not found"));
        
        Book book = bookRepository.findById(copy.bookId())
                .orElseThrow(() -> new BusinessException(ResultCode.PARAM_INVALID, "Book not found"));

        boolean wasAvailable = copy.isAvailable();
        try {
            copy.delete(Instant.now());
        } catch (IllegalStateException e) {
            throw new BusinessException(ResultCode.BOOK_COPY_BORROWED, e.getMessage());
        }
        book.removeCopy(wasAvailable);

        bookRepository.saveCopy(copy);
        bookRepository.save(book);
    }

    @Transactional
    public void deleteBook(Long bookId) {
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new BusinessException(ResultCode.PARAM_INVALID, "Book not found"));
        try {
            book.delete(Instant.now());
        } catch (IllegalStateException e) {
            throw new BusinessException(ResultCode.BOOK_HAS_UNRETURNED_COPY, e.getMessage());
        }
        for (BookCopy copy : bookRepository.findCopiesByBookId(bookId)) {
            copy.delete(Instant.now());
            bookRepository.saveCopy(copy);
        }
        bookRepository.save(book);
    }
}
