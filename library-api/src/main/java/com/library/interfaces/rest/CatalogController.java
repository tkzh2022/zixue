package com.library.interfaces.rest;

import com.library.domain.book.Book;
import com.library.domain.book.BookRepository;
import com.library.domain.shared.Result;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import com.library.interfaces.annotation.RateLimit;
import com.library.interfaces.dto.book.BookResponse;
import com.library.interfaces.dto.catalog.PageData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private final BookRepository bookRepository;

    public CatalogController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping("/books")
    @RateLimit(key = "ip", limit = 300, periodInSeconds = 60)
    public Result<PageData<BookResponse>> searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String normalizedCategory = category == null ? "" : category.trim().toLowerCase(Locale.ROOT);
        List<BookResponse> books = bookRepository.findAll().stream()
                .filter(book -> normalizedKeyword.isEmpty()
                        || book.isbn().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || book.title().toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || book.authorNames().stream().anyMatch(name -> name.toLowerCase(Locale.ROOT).contains(normalizedKeyword)))
                .filter(book -> normalizedCategory.isEmpty()
                        || book.categoryCodes().stream().anyMatch(code -> code.toLowerCase(Locale.ROOT).equals(normalizedCategory)))
                .map(this::toResponse)
                .toList();
        return Result.ok(PageData.from(books, page, size));
    }

    @GetMapping("/books/{id}")
    @RateLimit(key = "ip", limit = 300, periodInSeconds = 60)
    public Result<BookResponse> getBookDetail(@PathVariable Long id) {
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
