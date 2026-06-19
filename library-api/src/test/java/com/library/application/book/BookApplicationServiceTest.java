package com.library.application.book;

import com.library.application.book.command.CreateBookCommand;
import com.library.application.book.command.UpdateBookCommand;
import com.library.domain.book.Book;
import com.library.domain.book.BookCopy;
import com.library.domain.book.BookRepository;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookApplicationServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookApplicationService bookApplicationService;

    @BeforeEach
    void setUp() {
        bookApplicationService = new BookApplicationService(bookRepository);
    }

    @Test
    void createBook_Success() {
        CreateBookCommand cmd = new CreateBookCommand(
                "978-7-111-11111-1", "Test Book", "Test Pub", 2023,
                "A-1", "Summary", List.of("Author1"), List.of("C1")
        );

        when(bookRepository.existsByIsbn(cmd.isbn())).thenReturn(false);
        
        Book mockSavedBook = Book.create(cmd.isbn(), cmd.title(), cmd.publisher(), cmd.publishYear(),
                cmd.location(), cmd.summary(), cmd.authorNames(), cmd.categoryCodes(), Instant.now());
        mockSavedBook.assignId(100L);
        
        when(bookRepository.save(any(Book.class))).thenReturn(mockSavedBook);

        Long id = bookApplicationService.createBook(cmd);

        assertEquals(100L, id);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createBook_DuplicateIsbn_ThrowsException() {
        CreateBookCommand cmd = new CreateBookCommand(
                "978-7-111-11111-1", "Test Book", "Test Pub", 2023,
                "A-1", "Summary", List.of("Author1"), List.of("C1")
        );

        when(bookRepository.existsByIsbn(cmd.isbn())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> bookApplicationService.createBook(cmd));
        assertEquals(ResultCode.PARAM_INVALID, ex.resultCode());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void addCopy_Success() {
        Long bookId = 1L;
        String barcode = "B001";
        
        Book book = Book.create("123", "Title", "Pub", 2020, "Loc", "Sum", List.of(), List.of(), Instant.now());
        book.assignId(bookId);
        
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.existsCopyByBarcode(barcode)).thenReturn(false);
        
        BookCopy mockSavedCopy = BookCopy.create(bookId, barcode, Instant.now());
        mockSavedCopy.assignId(200L);
        when(bookRepository.saveCopy(any(BookCopy.class))).thenReturn(mockSavedCopy);

        bookApplicationService.addCopy(bookId, barcode);

        assertEquals(1, book.totalCopies());
        assertEquals(1, book.availableCopies());
        verify(bookRepository).saveCopy(any(BookCopy.class));
        verify(bookRepository).save(book);
    }
}
