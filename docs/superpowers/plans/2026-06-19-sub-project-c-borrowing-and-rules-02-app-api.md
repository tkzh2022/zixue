# Sub-project C 实现计划：借阅、规则与罚款 (2/3) - 应用层与接口层

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现借阅、规则、罚款的应用服务（事务边界、用例编排）以及 REST API 控制器。

---

## 任务 C5：应用层服务 (Application Services)

**Files:**
- Create: `library-api/src/main/java/com/library/application/borrow/BorrowApplicationService.java`
- Create: `library-api/src/main/java/com/library/application/fine/FineApplicationService.java`

- [ ] **Step 1: 编写 BorrowApplicationService**

```java
package com.library.application.borrow;
// ... imports
@Service
public class BorrowApplicationService {
    // Inject BorrowRecordRepository, BookRepository, ReaderRepository, BorrowRuleRepository, FineRecordRepository
    
    @Transactional
    public Long borrowBook(Long readerId, String barcode) {
        // 1. Check reader exists, is ACTIVE, has no UNPAID fines, has no OVERDUE records
        // 2. Check reader current borrow count < rule.maxBorrowCount
        // 3. Check copy exists, is IN_LIBRARY
        // 4. Update copy status to BORROWED
        // 5. Create BorrowRecord, calculate dueDate based on rule.maxBorrowDays
        // 6. Save all
    }
    
    @Transactional
    public void returnBook(Long borrowRecordId) {
        // 1. Find record, check status is BORROWING or OVERDUE
        // 2. Find copy, update status to IN_LIBRARY
        // 3. If OVERDUE, calculate fine and create FineRecord (UNPAID)
        // 4. Update record status to RETURNED
        // 5. Save all
    }
    
    @Transactional
    public void renewBook(Long borrowRecordId) {
        // 1. Find record, check status is BORROWING
        // 2. Check renewCount < rule.maxRenewCount
        // 3. Update dueDate, increment renewCount
        // 4. Save
    }
}
```

- [ ] **Step 2: 编写 FineApplicationService**

```java
package com.library.application.fine;
// ... imports
@Service
public class FineApplicationService {
    // Inject FineRecordRepository
    
    @Transactional
    public void payFine(Long fineRecordId, BigDecimal amount) {
        // Find fine, check status UNPAID
        // Check amount matches
        // Update status to PAID, set paidAt
        // Save
    }
}
```

- [ ] **Step 3: Commit**
```bash
git add library-api/src/main/java/com/library/application/
git commit -m "feat(app): application services for borrowing and fines"
```

## 任务 C6：REST Controllers (Admin/Librarian)

**Files:**
- Create: `library-api/src/main/java/com/library/interfaces/rest/RuleController.java`
- Create: `library-api/src/main/java/com/library/interfaces/rest/BorrowController.java`
- Create: `library-api/src/main/java/com/library/interfaces/rest/FineController.java`

- [ ] **Step 1: 编写 RuleController**
管理借阅规则 (CRUD)，`@RequireLibrarian`。

- [ ] **Step 2: 编写 BorrowController**
- `POST /api/v1/borrows` (Librarian: borrow book for reader)
- `PUT /api/v1/borrows/{id}/return` (Librarian: return book)
- `GET /api/v1/borrows` (Librarian: list all records)

- [ ] **Step 3: 编写 FineController**
- `GET /api/v1/fines` (Librarian: list all fines)
- `PUT /api/v1/fines/{id}/pay` (Librarian: process payment)

- [ ] **Step 4: Commit**
```bash
git add library-api/src/main/java/com/library/interfaces/rest/
git commit -m "feat(api): admin rest controllers for rules, borrows, fines"
```

## 任务 C7：REST Controllers (Reader)

**Files:**
- Create: `library-api/src/main/java/com/library/interfaces/rest/MyBorrowController.java`
- Create: `library-api/src/main/java/com/library/interfaces/rest/MyFineController.java`

- [ ] **Step 1: 编写 MyBorrowController**
- `GET /api/v1/my/borrows` (Reader: list own records)
- `PUT /api/v1/my/borrows/{id}/renew` (Reader: renew book)

- [ ] **Step 2: 编写 MyFineController**
- `GET /api/v1/my/fines` (Reader: list own fines)

- [ ] **Step 3: Commit**
```bash
git add library-api/src/main/java/com/library/interfaces/rest/My*
git commit -m "feat(api): reader rest controllers for own borrows and fines"
```
