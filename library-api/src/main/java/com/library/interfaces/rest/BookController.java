package com.library.interfaces.rest;

import com.library.application.book.BookApplicationService;
import com.library.application.book.command.CreateBookCommand;
import com.library.application.book.command.UpdateBookCommand;
import com.library.domain.book.Book;
import com.library.domain.book.BookRepository;
import com.library.domain.shared.Result;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import com.library.interfaces.dto.book.BookRequest;
import com.library.interfaces.dto.book.BookResponse;
import com.library.interfaces.dto.book.BookCopyResponse;
import com.library.interfaces.dto.catalog.PageData;
import com.library.interfaces.dto.book.CopyRequest;
import com.library.interfaces.security.RequireLibrarian;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/books")
@RequireLibrarian
public class BookController {

    private final BookApplicationService bookAppService;
    private final BookRepository bookRepository;

    public BookController(BookApplicationService bookAppService, BookRepository bookRepository) {
        this.bookAppService = bookAppService;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public Result<PageData<BookResponse>> getBooks(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<BookResponse> books = bookRepository.findAll().stream()
                .filter(book -> normalized.isEmpty()
                        || book.isbn().toLowerCase(Locale.ROOT).contains(normalized)
                        || book.title().toLowerCase(Locale.ROOT).contains(normalized)
                        || book.authorNames().stream().anyMatch(name -> name.toLowerCase(Locale.ROOT).contains(normalized)))
                .map(this::toResponse)
                .toList();
        return Result.ok(PageData.from(books, page, size));
    }

    @PostMapping
    public Result<Long> createBook(@Valid @RequestBody BookRequest request) {
        CreateBookCommand cmd = new CreateBookCommand(
                request.isbn(),
                request.title(),
                request.publisher(),
                request.publishYear(),
                request.location(),
                request.summary(),
                request.authorNames(),
                request.categoryCodes()
        );
        Long id = bookAppService.createBook(cmd);
        return Result.ok(id);
    }

    @PutMapping("/{id}")
    public Result<Void> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        UpdateBookCommand cmd = new UpdateBookCommand(
                id,
                request.title(),
                request.publisher(),
                request.publishYear(),
                request.location(),
                request.summary(),
                request.authorNames(),
                request.categoryCodes()
        );
        bookAppService.updateBook(cmd);
        return Result.ok(null);
    }

    @PostMapping("/{id}/copies")
    public Result<Void> addCopy(@PathVariable Long id, @Valid @RequestBody CopyRequest request) {
        bookAppService.addCopy(id, request.barcode());
        return Result.ok(null);
    }

    @DeleteMapping("/copies/{copyId}")
    public Result<Void> deleteCopy(@PathVariable Long copyId) {
        bookAppService.deleteCopy(copyId);
        return Result.ok(null);
    }

    @GetMapping("/{id}/copies")
    public Result<List<BookCopyResponse>> getCopies(@PathVariable Long id) {
        if (bookRepository.findById(id).isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "Book not found");
        }
        return Result.ok(bookRepository.findCopiesByBookId(id).stream()
                .map(copy -> new BookCopyResponse(copy.id(), copy.bookId(), copy.barcode(), copy.status()))
                .toList());
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBook(@PathVariable Long id) {
        bookAppService.deleteBook(id);
        return Result.ok(null);
    }

    @GetMapping("/{id}")
    public Result<BookResponse> getBook(@PathVariable Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.PARAM_INVALID, "Book not found"));

        return Result.ok(toResponse(book));
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
                book.id(),
                book.isbn(),
                book.title(),
                book.publisher(),
                book.publishYear(),
                book.totalCopies(),
                book.availableCopies(),
                book.location(),
                book.summary(),
                book.authorNames(),
                book.categoryCodes()
        );
    }
}
