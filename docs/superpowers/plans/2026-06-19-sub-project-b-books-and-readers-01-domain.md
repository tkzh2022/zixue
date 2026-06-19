# Sub-project B 实现计划：图书与读者 (1/3) - 领域与持久化

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现图书管理（包含复本、作者、分类）和读者管理的底层领域模型与数据库持久化。
**Architecture:** DDD 4层架构，领域层纯粹无依赖，基础设施层使用 Spring Data JPA。
**Tech Stack:** Java 17/21, Spring Boot 3.2, Spring Data JPA, Flyway, MapStruct.

---

## 任务 B1：Flyway 迁移脚本 V3 与 V4

**Files:**
- Create: `library-api/src/main/resources/db/migration/V3__init_books.sql`
- Create: `library-api/src/main/resources/db/migration/V4__init_readers.sql`

- [ ] **Step 1: 编写图书相关表脚本**

```sql
-- V3__init_books.sql
CREATE TABLE author (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_author_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    parent_id BIGINT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE book (
    id BIGINT NOT NULL AUTO_INCREMENT,
    isbn VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    publisher VARCHAR(128) NULL,
    publish_year INT NULL,
    total_copies INT NOT NULL DEFAULT 0,
    available_copies INT NOT NULL DEFAULT 0,
    location VARCHAR(64) NULL,
    summary TEXT NULL,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    deleted_at DATETIME(3) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_book_isbn (isbn),
    FULLTEXT KEY ft_book_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE book_author (
    book_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE book_category (
    book_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE book_copy (
    id BIGINT NOT NULL AUTO_INCREMENT,
    book_id BIGINT NOT NULL,
    barcode VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    deleted_at DATETIME(3) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_book_copy_barcode (barcode),
    KEY idx_copy_book_status (book_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: 编写读者相关表脚本**

```sql
-- V4__init_readers.sql
CREATE TABLE reader (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_account_id BIGINT NOT NULL,
    reader_no VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    phone VARCHAR(32) NULL,
    email VARCHAR(128) NULL,
    status VARCHAR(16) NOT NULL,
    register_date DATE NOT NULL,
    deleted_at DATETIME(3) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reader_no (reader_no),
    UNIQUE KEY uk_reader_user_account (user_account_id),
    KEY idx_reader_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 3: Commit**
```bash
git add library-api/src/main/resources/db/migration/
git commit -m "feat(db): flyway migrations for books and readers"
```

## 任务 B2：图书领域模型与接口

**Files:**
- Create: `library-api/src/main/java/com/library/domain/book/CopyStatus.java`
- Create: `library-api/src/main/java/com/library/domain/book/BookCopy.java`
- Create: `library-api/src/main/java/com/library/domain/book/Book.java`
- Create: `library-api/src/main/java/com/library/domain/book/BookRepository.java`

- [ ] **Step 1: 创建图书领域对象**

```java
// CopyStatus.java
package com.library.domain.book;
public enum CopyStatus { IN_LIBRARY, BORROWED, LOST, MAINTENANCE }

// BookCopy.java
package com.library.domain.book;
import java.time.Instant;
public class BookCopy {
    private Long id;
    private Long bookId;
    private String barcode;
    private CopyStatus status;
    private Instant createdAt;
    private Instant deletedAt;
    // ... getters, setters, static factory methods (create, restore)
    // business methods: markLost(), markMaintenance(), isAvailable()
}

// Book.java
package com.library.domain.book;
import java.time.Instant;
import java.util.List;
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
    private List<String> authorNames; // Simplified for domain logic
    private List<String> categoryCodes;
    // ... getters, setters, static factory methods
    // business methods: addCopy(), removeCopy(), borrowCopy(), returnCopy()
}

// BookRepository.java
package com.library.domain.book;
import java.util.Optional;
public interface BookRepository {
    Optional<Book> findById(Long id);
    Optional<Book> findByIsbn(String isbn);
    Book save(Book book);
    boolean existsByIsbn(String isbn);
    // Copy methods
    Optional<BookCopy> findCopyByBarcode(String barcode);
    BookCopy saveCopy(BookCopy copy);
}
```

- [ ] **Step 2: Commit**
```bash
git add library-api/src/main/java/com/library/domain/book/
git commit -m "feat(domain): book and book_copy aggregates"
```

## 任务 B3：读者领域模型与接口

**Files:**
- Create: `library-api/src/main/java/com/library/domain/reader/ReaderStatus.java`
- Create: `library-api/src/main/java/com/library/domain/reader/Reader.java`
- Create: `library-api/src/main/java/com/library/domain/reader/ReaderRepository.java`

- [ ] **Step 1: 创建读者领域对象**

```java
// ReaderStatus.java
package com.library.domain.reader;
public enum ReaderStatus { ACTIVE, DISABLED }

// Reader.java
package com.library.domain.reader;
import java.time.LocalDate;
import java.time.Instant;
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
    // ... getters, setters, factories
    // business methods: disable(), enable(), isActive()
}

// ReaderRepository.java
package com.library.domain.reader;
import java.util.Optional;
public interface ReaderRepository {
    Optional<Reader> findById(Long id);
    Optional<Reader> findByReaderNo(String readerNo);
    Optional<Reader> findByUserAccountId(Long userAccountId);
    Reader save(Reader reader);
    boolean existsByReaderNo(String readerNo);
}
```

- [ ] **Step 2: Commit**
```bash
git add library-api/src/main/java/com/library/domain/reader/
git commit -m "feat(domain): reader aggregate"
```

## 任务 B4：JPA 实体与仓储实现

**Files:**
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/book/BookJpaEntity.java` (及 Copy, Author, Category)
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/book/BookJpaRepository.java`
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/book/BookRepositoryImpl.java`
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/reader/ReaderJpaEntity.java`
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/reader/ReaderJpaRepository.java`
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/reader/ReaderRepositoryImpl.java`

- [ ] **Step 1: 编写 JPA 实体与 Repository Impl**
实现上述实体的 JPA 映射。对于 Book 的 author 和 category，可以使用 `@ElementCollection` 或 `@ManyToMany` 映射。为了简化 MVP，这里推荐在 `BookJpaEntity` 中使用 `@ManyToMany` 关联 `AuthorJpaEntity` 和 `CategoryJpaEntity`。

- [ ] **Step 2: Commit**
```bash
git add library-api/src/main/java/com/library/infrastructure/persistence/jpa/
git commit -m "feat(infra): jpa repositories for books and readers"
```
