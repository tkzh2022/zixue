package com.library.application.borrow;

import com.library.application.book.BookApplicationService;
import com.library.application.book.command.CreateBookCommand;
import com.library.domain.reader.Reader;
import com.library.domain.reader.ReaderRepository;
import com.library.domain.user.UserAccount;
import com.library.domain.user.UserAccountRepository;
import com.library.domain.user.UserRole;
import com.library.support.JpaIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class BorrowConcurrencyIT extends JpaIntegrationTestBase {

    @Autowired BorrowApplicationService borrowApplicationService;
    @Autowired BookApplicationService bookApplicationService;
    @Autowired UserAccountRepository userAccountRepository;
    @Autowired ReaderRepository readerRepository;

    @Test
    void sameCopyCanOnlyBeBorrowedOnceUnderConcurrency() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String readerNo1 = "RA" + suffix;
        String readerNo2 = "RB" + suffix;
        createReader("user-a-" + suffix, readerNo1);
        createReader("user-b-" + suffix, readerNo2);

        String isbn = "ISBN-" + suffix;
        String barcode = "COPY-" + suffix;
        Long bookId = bookApplicationService.createBook(new CreateBookCommand(
                isbn, "Concurrent Book", "Test", 2026, "T-1", "test", List.of(), List.of()));
        bookApplicationService.addCopy(bookId, barcode);

        var executor = Executors.newFixedThreadPool(2);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try {
            var first = executor.submit(() -> attemptBorrow(readerNo1, barcode, ready, start));
            var second = executor.submit(() -> attemptBorrow(readerNo2, barcode, ready, start));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            long successes = List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS))
                    .stream().filter(Boolean::booleanValue).count();
            assertThat(successes).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private void createReader(String username, String readerNo) {
        UserAccount account = UserAccount.register(username, "test-hash", UserRole.READER, Instant.now());
        userAccountRepository.save(account);
        readerRepository.save(Reader.create(
                account.id(), readerNo, username, null, null, LocalDate.now()));
    }

    private boolean attemptBorrow(String readerNo, String barcode,
                                  CountDownLatch ready, CountDownLatch start) throws InterruptedException {
        ready.countDown();
        start.await();
        try {
            borrowApplicationService.borrowBook(readerNo, barcode);
            return true;
        } catch (RuntimeException expected) {
            return false;
        }
    }
}
