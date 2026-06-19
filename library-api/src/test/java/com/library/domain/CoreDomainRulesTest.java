package com.library.domain;

import com.library.domain.book.Book;
import com.library.domain.book.BookCopy;
import com.library.domain.borrow.BorrowRecord;
import com.library.domain.fine.FineRecord;
import com.library.domain.fine.FineStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoreDomainRulesTest {

    @Test
    void inventoryNeverBecomesNegativeOrExceedsTotal() {
        Book book = Book.create("isbn", "title", null, 2024, null, null, List.of(), List.of(), Instant.now());
        assertThrows(IllegalStateException.class, book::borrowCopy);

        book.addCopy();
        book.borrowCopy();
        assertEquals(0, book.availableCopies());
        book.returnCopy();
        assertEquals(1, book.availableCopies());
        assertThrows(IllegalStateException.class, book::returnCopy);
    }

    @Test
    void borrowedCopyCannotBeDeleted() {
        BookCopy copy = BookCopy.create(1L, "BC-1", Instant.now());
        copy.markBorrowed();
        assertThrows(IllegalStateException.class, () -> copy.delete(Instant.now()));
    }

    @Test
    void renewalChangesDueDateOnceAndHonorsLimit() {
        Instant now = Instant.now();
        BorrowRecord record = BorrowRecord.create(1L, 2L, 30, now);
        var originalDueDate = record.dueDate();

        record.renew(1, 30, now.plusSeconds(1));

        assertEquals(originalDueDate.plusDays(30), record.dueDate());
        assertEquals(1, record.renewCount());
        assertThrows(IllegalStateException.class, () -> record.renew(1, 30, now.plusSeconds(2)));
    }

    @Test
    void fineRequiresExactAmountAndCannotBePaidTwice() {
        FineRecord fine = FineRecord.create(1L, 2L, new BigDecimal("3.50"), "overdue", Instant.now());
        assertThrows(IllegalArgumentException.class,
                () -> fine.pay(new BigDecimal("3.49"), Instant.now()));

        fine.pay(new BigDecimal("3.50"), Instant.now());

        assertEquals(FineStatus.PAID, fine.status());
        assertThrows(IllegalStateException.class,
                () -> fine.pay(new BigDecimal("3.50"), Instant.now()));
    }
}
