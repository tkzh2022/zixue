# Sub-project C 实现计划：借阅、规则与罚款 (1/3) - 领域与持久化

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现借阅规则、借阅记录和罚款记录的底层领域模型与数据库持久化。
**Architecture:** DDD 4层架构。
**Tech Stack:** Java 17/21, Spring Boot 3.2, Spring Data JPA, Flyway.

---

## 任务 C1：Flyway 迁移脚本 V5

**Files:**
- Create: `library-api/src/main/resources/db/migration/V5__init_borrowing.sql`

- [ ] **Step 1: 编写借阅与罚款相关表脚本**

```sql
-- V5__init_borrowing.sql
CREATE TABLE borrow_rule (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reader_type VARCHAR(32) NOT NULL,
    max_borrow_days INT NOT NULL,
    max_borrow_count INT NOT NULL,
    max_renew_count INT NOT NULL,
    fine_per_day DECIMAL(10, 2) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_rule_reader_type (reader_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅规则表';

CREATE TABLE borrow_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reader_id BIGINT NOT NULL,
    book_copy_id BIGINT NOT NULL,
    borrow_time DATETIME(3) NOT NULL,
    due_date DATE NOT NULL,
    return_time DATETIME(3) NULL,
    renew_count INT NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_borrow_reader (reader_id),
    KEY idx_borrow_copy (book_copy_id),
    KEY idx_borrow_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

CREATE TABLE fine_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    borrow_record_id BIGINT NOT NULL,
    reader_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    reason VARCHAR(128) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    paid_at DATETIME(3) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_fine_borrow (borrow_record_id),
    KEY idx_fine_reader (reader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='罚款记录表';
```

- [ ] **Step 2: Commit**
```bash
git add library-api/src/main/resources/db/migration/
git commit -m "feat(db): flyway migrations for borrowing and fines"
```

## 任务 C2：借阅规则领域模型

**Files:**
- Create: `library-api/src/main/java/com/library/domain/borrow/BorrowRule.java`
- Create: `library-api/src/main/java/com/library/domain/borrow/BorrowRuleRepository.java`

- [ ] **Step 1: 创建 BorrowRule 领域对象**

```java
package com.library.domain.borrow;
import java.math.BigDecimal;
import java.time.Instant;
public class BorrowRule {
    private Long id;
    private String readerType;
    private int maxBorrowDays;
    private int maxBorrowCount;
    private int maxRenewCount;
    private BigDecimal finePerDay;
    // ... getters, setters, factories, updateInfo()
}
```

- [ ] **Step 2: Commit**
```bash
git add library-api/src/main/java/com/library/domain/borrow/BorrowRule*
git commit -m "feat(domain): borrow rule aggregate"
```

## 任务 C3：借阅与罚款领域模型

**Files:**
- Create: `library-api/src/main/java/com/library/domain/borrow/BorrowStatus.java`
- Create: `library-api/src/main/java/com/library/domain/borrow/BorrowRecord.java`
- Create: `library-api/src/main/java/com/library/domain/borrow/BorrowRecordRepository.java`
- Create: `library-api/src/main/java/com/library/domain/fine/FineStatus.java`
- Create: `library-api/src/main/java/com/library/domain/fine/FineRecord.java`
- Create: `library-api/src/main/java/com/library/domain/fine/FineRecordRepository.java`

- [ ] **Step 1: 创建 BorrowRecord 领域对象**

```java
package com.library.domain.borrow;
public enum BorrowStatus { BORROWING, RETURNED, OVERDUE, LOST }

public class BorrowRecord {
    private Long id;
    private Long readerId;
    private Long bookCopyId;
    private Instant borrowTime;
    private LocalDate dueDate;
    private Instant returnTime;
    private int renewCount;
    private BorrowStatus status;
    // ... business methods: returnBook(), renew(), markOverdue(), markLost()
}
```

- [ ] **Step 2: 创建 FineRecord 领域对象**

```java
package com.library.domain.fine;
public enum FineStatus { UNPAID, PAID, WAIVED }

public class FineRecord {
    private Long id;
    private Long borrowRecordId;
    private Long readerId;
    private BigDecimal amount;
    private String reason;
    private FineStatus status;
    private Instant paidAt;
    // ... business methods: pay(), waive()
}
```

- [ ] **Step 3: Commit**
```bash
git add library-api/src/main/java/com/library/domain/borrow/ library-api/src/main/java/com/library/domain/fine/
git commit -m "feat(domain): borrow record and fine record aggregates"
```

## 任务 C4：JPA 实体与仓储实现

**Files:**
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/borrow/BorrowRuleJpaEntity.java` (及 Repository, Impl)
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/borrow/BorrowRecordJpaEntity.java` (及 Repository, Impl)
- Create: `library-api/src/main/java/com/library/infrastructure/persistence/jpa/fine/FineRecordJpaEntity.java` (及 Repository, Impl)

- [ ] **Step 1: 编写 JPA 实体与 Repository Impl**
实现上述三个聚合根的 JPA 映射和 Repository 接口实现。

- [ ] **Step 2: Commit**
```bash
git add library-api/src/main/java/com/library/infrastructure/persistence/jpa/
git commit -m "feat(infra): jpa repositories for borrowing and fines"
```
