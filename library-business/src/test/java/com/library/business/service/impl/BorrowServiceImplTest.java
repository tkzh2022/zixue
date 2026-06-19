package com.library.business.service.impl;

import com.library.domain.borrow.BorrowRule;
import com.library.domain.borrow.BorrowRecordRepository;
import com.library.domain.borrow.BorrowRuleRepository;
import com.library.domain.book.BookRepository;
import com.library.domain.fine.FineRecordRepository;
import com.library.domain.reader.Reader;
import com.library.domain.reader.ReaderRepository;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowServiceImplTest {

    @Mock private BorrowRecordRepository borrowRecordRepository;
    @Mock private BookRepository bookRepository;
    @Mock private ReaderRepository readerRepository;
    @Mock private BorrowRuleRepository borrowRuleRepository;
    @Mock private FineRecordRepository fineRecordRepository;

    private BorrowServiceImpl borrowService;

    @BeforeEach
    void setUp() {
        borrowService = new BorrowServiceImpl(
                borrowRecordRepository, bookRepository, readerRepository, borrowRuleRepository, fineRecordRepository
        );
    }

    @Test
    void testBorrowBook_ExceedsLimit() {
        String readerNo = "R001";
        String barcode = "B001";

        Reader reader = Reader.create(1L, readerNo, "John", "123", "a@b.com", LocalDate.now());
        reader.assignId(10L);
        when(readerRepository.findByReaderNo(readerNo)).thenReturn(Optional.of(reader));
        when(fineRecordRepository.hasUnpaidFines(10L)).thenReturn(false);
        when(borrowRecordRepository.hasOverdueRecords(10L)).thenReturn(false);

        BorrowRule rule = BorrowRule.create("DEFAULT", 30, 5, 1, BigDecimal.valueOf(0.5), Instant.now());
        when(borrowRuleRepository.findByReaderType("DEFAULT")).thenReturn(Optional.of(rule));
        when(borrowRecordRepository.countUnreturnedByReaderId(10L)).thenReturn(5);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> borrowService.borrowBook(readerNo, barcode)
        );

        assertEquals(ResultCode.READER_BORROW_LIMIT_EXCEEDED, ex.resultCode());
    }
}
