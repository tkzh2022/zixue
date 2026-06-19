package com.library.business.service.impl;

import com.library.business.service.BorrowService;
import com.library.domain.book.Book;
import com.library.domain.book.BookCopy;
import com.library.domain.book.BookRepository;
import com.library.domain.borrow.BorrowRecord;
import com.library.domain.borrow.BorrowRecordRepository;
import com.library.domain.borrow.BorrowRule;
import com.library.domain.borrow.BorrowRuleRepository;
import com.library.domain.fine.FineRecordRepository;
import com.library.domain.reader.Reader;
import com.library.domain.reader.ReaderRepository;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;

import java.time.Instant;

public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final BorrowRuleRepository borrowRuleRepository;
    private final FineRecordRepository fineRecordRepository;

    public BorrowServiceImpl(BorrowRecordRepository borrowRecordRepository,
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

    @Override
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

        BorrowRule rule = borrowRuleRepository.findByReaderType("DEFAULT")
                .orElseThrow(() -> new BusinessException(ResultCode.SYSTEM_ERROR, "Default borrow rule not found"));

        int currentBorrowCount = borrowRecordRepository.countUnreturnedByReaderId(reader.id());
        if (currentBorrowCount >= rule.maxBorrowCount()) {
            throw new BusinessException(ResultCode.READER_BORROW_LIMIT_EXCEEDED);
        }

        BookCopy copy = bookRepository.findCopyByBarcode(barcode)
                .orElseThrow(() -> new BusinessException(ResultCode.PARAM_INVALID, "Copy not found"));

        if (!copy.isAvailable()) {
            throw new BusinessException(ResultCode.BORROWING_NO_AVAILABLE_COPY);
        }

        Book book = bookRepository.findById(copy.bookId())
                .orElseThrow(() -> new BusinessException(ResultCode.SYSTEM_ERROR, "Book not found"));

        copy.markBorrowed();
        book.borrowCopy();

        Instant now = Instant.now();
        BorrowRecord record = BorrowRecord.create(reader.id(), copy.id(), rule.maxBorrowDays(), now);

        bookRepository.saveCopy(copy);
        bookRepository.save(book);
        return borrowRecordRepository.save(record).id();
    }
}
