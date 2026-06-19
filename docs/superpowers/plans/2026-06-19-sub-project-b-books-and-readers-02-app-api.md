# Sub-project B 实现计划：图书与读者 (2/3) - 应用层与接口层

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现图书和读者的应用服务（事务边界、用例编排）以及 REST API 控制器。

---

## 任务 B5：应用层服务 (Application Services)

**Files:**
- Create: `library-api/src/main/java/com/library/application/book/BookApplicationService.java`
- Create: `library-api/src/main/java/com/library/application/book/command/CreateBookCommand.java` (及 Update, AddCopy 等)
- Create: `library-api/src/main/java/com/library/application/reader/ReaderApplicationService.java`
- Create: `library-api/src/main/java/com/library/application/reader/command/CreateReaderCommand.java`

- [ ] **Step 1: 编写 BookApplicationService**

```java
package com.library.application.book;
// ... imports
@Service
public class BookApplicationService {
    private final BookRepository bookRepository;
    
    @Transactional
    public Long createBook(CreateBookCommand cmd) {
        if (bookRepository.existsByIsbn(cmd.isbn())) {
            throw new BusinessException(ResultCode.BOOK_ISBN_DUPLICATED);
        }
        // map cmd to domain Book, save and return id
    }
    
    @Transactional
    public void addCopy(Long bookId, String barcode) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BusinessException(ResultCode.PARAM_INVALID, "Book not found"));
        // check barcode duplicate
        // create BookCopy, increment book.totalCopies and availableCopies
        // save book and copy
    }
    // ... updateBook, deleteBook (check availableCopies == totalCopies), removeCopy
}
```

- [ ] **Step 2: 编写 ReaderApplicationService**

```java
package com.library.application.reader;
// ... imports
@Service
public class ReaderApplicationService {
    private final ReaderRepository readerRepository;
    private final UserAccountRepository userAccountRepository;
    
    @Transactional
    public Long createReader(CreateReaderCommand cmd) {
        if (readerRepository.existsByReaderNo(cmd.readerNo())) {
            throw new BusinessException(ResultCode.READER_NO_DUPLICATED);
        }
        // check userAccount exists and is READER
        // create Reader domain object, save and return id
    }
    
    @Transactional
    public void updateStatus(Long readerId, ReaderStatus status) {
        Reader reader = readerRepository.findById(readerId)
            .orElseThrow(() -> new BusinessException(ResultCode.READER_NOT_FOUND));
        // update status, save
    }
}
```

- [ ] **Step 3: Commit**
```bash
git add library-api/src/main/java/com/library/application/
git commit -m "feat(app): application services for books and readers"
```

## 任务 B6：REST Controllers (Admin)

**Files:**
- Create: `library-api/src/main/java/com/library/interfaces/rest/BookController.java`
- Create: `library-api/src/main/java/com/library/interfaces/rest/ReaderController.java`
- Create: `library-api/src/main/java/com/library/interfaces/dto/book/BookRequest.java` (及 CopyRequest, BookResponse 等)
- Create: `library-api/src/main/java/com/library/interfaces/dto/reader/ReaderRequest.java` (及 ReaderResponse 等)

- [ ] **Step 1: 编写 BookController**

```java
package com.library.interfaces.rest;
// ... imports
@RestController
@RequestMapping("/api/v1/books")
@RequireLibrarian // Admin only
public class BookController {
    private final BookApplicationService bookAppService;
    // ... constructor
    
    @PostMapping
    public Result<Long> createBook(@Valid @RequestBody BookRequest request) {
        // map to command, call service
    }
    
    @PostMapping("/{id}/copies")
    public Result<Void> addCopy(@PathVariable Long id, @Valid @RequestBody CopyRequest request) {
        // call service
    }
    
    // ... GET /books (with pagination/search), GET /books/{id}, PUT /books/{id}, DELETE /books/{id}
    // ... DELETE /copies/{id}
}
```

- [ ] **Step 2: 编写 ReaderController**

```java
package com.library.interfaces.rest;
// ... imports
@RestController
@RequestMapping("/api/v1/readers")
@RequireLibrarian
public class ReaderController {
    private final ReaderApplicationService readerAppService;
    // ... constructor
    
    @PostMapping
    public Result<Long> createReader(@Valid @RequestBody ReaderRequest request) {
        // call service
    }
    
    // ... GET /readers, GET /readers/{id}, PUT /readers/{id}, PUT /readers/{id}/status
}
```

- [ ] **Step 3: Commit**
```bash
git add library-api/src/main/java/com/library/interfaces/rest/
git add library-api/src/main/java/com/library/interfaces/dto/
git commit -m "feat(api): rest controllers for admin book and reader management"
```

## 任务 B7：REST Controller (Public Catalog)

**Files:**
- Create: `library-api/src/main/java/com/library/interfaces/rest/CatalogController.java`

- [ ] **Step 1: 编写 CatalogController**

```java
package com.library.interfaces.rest;
// ... imports
@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {
    // Inject BookRepository or a specific CatalogQueryService
    
    @GetMapping("/books")
    @RateLimit(key = "ip", limit = 300, periodInSeconds = 60)
    public Result<PageData<BookResponse>> searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Public search, return paginated results
    }
    
    @GetMapping("/books/{id}")
    public Result<BookDetailResponse> getBookDetail(@PathVariable Long id) {
        // Public detail
    }
}
```

- [ ] **Step 2: Commit**
```bash
git add library-api/src/main/java/com/library/interfaces/rest/CatalogController.java
git commit -m "feat(api): public catalog controller"
```
