package com.library.application.borrow;

import com.library.domain.book.Book;
import com.library.domain.book.BookCopy;
import com.library.domain.book.BookRepository;
import com.library.domain.borrow.BorrowRecord;
import com.library.domain.borrow.BorrowRecordRepository;
import com.library.domain.borrow.BorrowRule;
import com.library.domain.borrow.BorrowRuleRepository;
import com.library.domain.fine.FineRecord;
import com.library.domain.fine.FineRecordRepository;
import com.library.domain.reader.Reader;
import com.library.domain.reader.ReaderRepository;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
public class BorrowApplicationService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final BorrowRuleRepository borrowRuleRepository;
    private final FineRecordRepository fineRecordRepository;

    public BorrowApplicationService(BorrowRecordRepository borrowRecordRepository,
                                    BookRepository bookRepository,
                                    ReaderRepository readerRepository,
                                    BorrowRuleRepository borrowRuleRepository,
                                    FineRecordRepository fineRecordRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
        this.readerRepository = readerRepository;
        this.borrowRuleRepository = borrowRuleRepository;
        this.fineRecordRepository = fineRecordRepository;
    }

    @Transactional
    public Long borrowBook(String readerNo, String barcode) {
        Reader reader = readerRepository.findByReaderNo(readerNo)
                .orElseThrow(() -> new BusinessException(ResultCode.READER_NOT_FOUND));

        if (!reader.isActive()) {
            throw new BusinessException(ResultCode.READER_DISABLED);
        }

        if (fineRecordRepository.hasUnpaidFines(reader.id())) {
            throw new BusinessException(ResultCode.BORROWING_FINE_UNPAID);
        }

        if (borrowRecordRepository.hasOverdueRecords(reader.id())) {
            throw new BusinessException(ResultCode.READER_HAS_OVERDUE);
        }

        BorrowRule rule = borrowRuleRepository.findByReaderType("DEFAULT") // Simplified for MVP
                .orElseThrow(() -> new BusinessException(ResultCode.SYSTEM_ERROR, "Default borrow rule not found"));

        int currentBorrowCount = borrowRecordRepository.countUnreturnedByReaderId(reader.id());
        if (currentBorrowCount >= rule.maxBorrowCount()) {
            throw new BusinessException(ResultCode.READER_BORROW_LIMIT_EXCEEDED);
        }

        BookCopy copy = bookRepository.findCopyByBarcodeForUpdate(barcode)
                .orElseThrow(() -> new BusinessException(ResultCode.PARAM_INVALID, "Copy not found"));

        if (!copy.isAvailable()) {
            throw new BusinessException(ResultCode.BORROWING_NO_AVAILABLE_COPY);
        }

        Book book = bookRepository.findByIdForUpdate(copy.bookId())
                .orElseThrow(() -> new BusinessException(ResultCode.SYSTEM_ERROR, "Book not found"));

        copy.markBorrowed();
        book.borrowCopy();

        Instant now = Instant.now();
        BorrowRecord record = BorrowRecord.create(reader.id(), copy.id(), rule.maxBorrowDays(), now);

        bookRepository.saveCopy(copy);
        bookRepository.save(book);
        return borrowRecordRepository.save(record).id();
    }

    @Transactional
    public void returnBook(String barcode) {
        BookCopy copy = bookRepository.findCopyByBarcodeForUpdate(barcode)
                .orElseThrow(() -> new BusinessException(ResultCode.PARAM_INVALID, "Copy not found"));

        BorrowRecord record = borrowRecordRepository.findActiveByBookCopyIdForUpdate(copy.id())
                .orElseThrow(() -> new BusinessException(ResultCode.BORROWING_NOT_RETURNABLE));

        Book book = bookRepository.findByIdForUpdate(copy.bookId())
                .orElseThrow(() -> new BusinessException(ResultCode.SYSTEM_ERROR, "Book not found"));

        Instant now = Instant.now();
        LocalDate today = LocalDate.ofInstant(now, ZoneId.systemDefault());

        if (record.isOverdue(today)) {
            BorrowRule rule = borrowRuleRepository.findByReaderType("DEFAULT")
                    .orElseThrow(() -> new BusinessException(ResultCode.SYSTEM_ERROR, "Default borrow rule not found"));
            
            long overdueDays = ChronoUnit.DAYS.between(record.dueDate(), today);
            BigDecimal fineAmount = rule.finePerDay().multiply(BigDecimal.valueOf(overdueDays));
            
            FineRecord fine = FineRecord.create(record.id(), record.readerId(), fineAmount, "Overdue fine for " + overdueDays + " days", now);
            fineRecordRepository.save(fine);
        }

        record.returnBook(now);
        copy.markReturned();
        book.returnCopy();

        borrowRecordRepository.save(record);
        bookRepository.saveCopy(copy);
        bookRepository.save(book);
    }

    @Transactional
    public void renewBook(Long userAccountId, Long borrowRecordId) {
        Reader reader = readerRepository.findByUserAccountId(userAccountId)
                .orElseThrow(() -> new BusinessException(ResultCode.READER_NOT_FOUND));

        BorrowRecord record = borrowRecordRepository.findByIdForUpdate(borrowRecordId)
                .orElseThrow(() -> new BusinessException(ResultCode.PARAM_INVALID, "Borrow record not found"));

        if (!record.readerId().equals(reader.id())) {
            throw new BusinessException(ResultCode.AUTH_FORBIDDEN);
        }

        BorrowRule rule = borrowRuleRepository.findByReaderType("DEFAULT")
                .orElseThrow(() -> new BusinessException(ResultCode.SYSTEM_ERROR, "Default borrow rule not found"));

        if (record.renewCount() >= rule.maxRenewCount()) {
            throw new BusinessException(ResultCode.BORROWING_RENEW_LIMIT_EXCEEDED);
        }

        if (fineRecordRepository.hasUnpaidFines(record.readerId())) {
            throw new BusinessException(ResultCode.BORROWING_FINE_UNPAID);
        }

        try {
            record.renew(rule.maxRenewCount(), rule.maxBorrowDays(), Instant.now());
        } catch (IllegalStateException e) {
            throw new BusinessException(ResultCode.BORROWING_NOT_RENEWABLE, e.getMessage());
        }
        borrowRecordRepository.save(record);
    }
}
