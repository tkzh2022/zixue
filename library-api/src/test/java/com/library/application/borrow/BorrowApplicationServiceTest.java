package com.library.application.borrow;

import com.library.domain.book.Book;
import com.library.domain.book.BookCopy;
import com.library.domain.book.BookRepository;
import com.library.domain.borrow.BorrowRecord;
import com.library.domain.borrow.BorrowRecordRepository;
import com.library.domain.borrow.BorrowRule;
import com.library.domain.borrow.BorrowRuleRepository;
import com.library.domain.fine.FineRecordRepository;
import com.library.domain.fine.FineRecord;
import com.library.domain.fine.FineStatus;
import com.library.domain.borrow.BorrowStatus;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowApplicationServiceTest {

    @Mock private BorrowRecordRepository borrowRecordRepository;
    @Mock private BookRepository bookRepository;
    @Mock private ReaderRepository readerRepository;
    @Mock private BorrowRuleRepository borrowRuleRepository;
    @Mock private FineRecordRepository fineRecordRepository;

    private BorrowApplicationService borrowApplicationService;

    @BeforeEach
    void setUp() {
        borrowApplicationService = new BorrowApplicationService(
                borrowRecordRepository, bookRepository, readerRepository, borrowRuleRepository, fineRecordRepository
        );
    }

    @Test
    void borrowBook_Success() {
        String readerNo = "R001";
        String barcode = "B001";

        Reader reader = Reader.create(1L, readerNo, "John", "123", "a@b.com", LocalDate.now());
        reader.assignId(10L);
        when(readerRepository.findByReaderNo(readerNo)).thenReturn(Optional.of(reader));
        when(fineRecordRepository.hasUnpaidFines(10L)).thenReturn(false);
        when(borrowRecordRepository.hasOverdueRecords(10L)).thenReturn(false);

        BorrowRule rule = BorrowRule.create("DEFAULT", 30, 5, 1, BigDecimal.valueOf(0.5), Instant.now());
        when(borrowRuleRepository.findByReaderType("DEFAULT")).thenReturn(Optional.of(rule));
        when(borrowRecordRepository.countUnreturnedByReaderId(10L)).thenReturn(0);

        BookCopy copy = BookCopy.create(100L, barcode, Instant.now());
        copy.assignId(20L);
        when(bookRepository.findCopyByBarcodeForUpdate(barcode)).thenReturn(Optional.of(copy));

        Book book = Book.create("123", "Title", "Pub", 2020, "Loc", "Sum", List.of(), List.of(), Instant.now());
        book.assignId(100L);
        book.addCopy(); // Make availableCopies = 1
        when(bookRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(book));

        BorrowRecord mockSavedRecord = BorrowRecord.create(10L, 20L, 30, Instant.now());
        mockSavedRecord.assignId(30L);
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(mockSavedRecord);

        Long id = borrowApplicationService.borrowBook(readerNo, barcode);

        assertEquals(30L, id);
        assertEquals(0, book.availableCopies());
        assertEquals(com.library.domain.book.CopyStatus.BORROWED, copy.status());
        verify(bookRepository).saveCopy(copy);
        verify(bookRepository).save(book);
        verify(borrowRecordRepository).save(any(BorrowRecord.class));
    }

    @Test
    void borrowBook_ReaderDisabled_ThrowsException() {
        String readerNo = "R001";
        Reader reader = Reader.create(1L, readerNo, "John", "123", "a@b.com", LocalDate.now());
        reader.disable();
        when(readerRepository.findByReaderNo(readerNo)).thenReturn(Optional.of(reader));

        BusinessException ex = assertThrows(BusinessException.class, () -> borrowApplicationService.borrowBook(readerNo, "B001"));
        assertEquals(ResultCode.READER_DISABLED, ex.resultCode());
    }

    @Test
    void borrowBook_UnpaidFine_ThrowsException() {
        Reader reader = Reader.create(1L, "R001", "John", "123", "a@b.com", LocalDate.now());
        reader.assignId(10L);
        when(readerRepository.findByReaderNo("R001")).thenReturn(Optional.of(reader));
        when(fineRecordRepository.hasUnpaidFines(10L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowApplicationService.borrowBook("R001", "B001"));

        assertEquals(ResultCode.BORROWING_FINE_UNPAID, ex.resultCode());
        verifyNoInteractions(bookRepository);
    }

    @Test
    void borrowBook_AtLimit_ThrowsException() {
        Reader reader = Reader.create(1L, "R001", "John", "123", "a@b.com", LocalDate.now());
        reader.assignId(10L);
        when(readerRepository.findByReaderNo("R001")).thenReturn(Optional.of(reader));
        when(fineRecordRepository.hasUnpaidFines(10L)).thenReturn(false);
        when(borrowRecordRepository.hasOverdueRecords(10L)).thenReturn(false);
        BorrowRule rule = BorrowRule.create("DEFAULT", 30, 5, 1, BigDecimal.valueOf(0.5), Instant.now());
        when(borrowRuleRepository.findByReaderType("DEFAULT")).thenReturn(Optional.of(rule));
        when(borrowRecordRepository.countUnreturnedByReaderId(10L)).thenReturn(5);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowApplicationService.borrowBook("R001", "B001"));

        assertEquals(ResultCode.READER_BORROW_LIMIT_EXCEEDED, ex.resultCode());
    }

    @Test
    void returnBook_Overdue_CreatesFineAndRestoresInventory() {
        Instant now = Instant.now();
        BookCopy copy = BookCopy.create(100L, "B001", now);
        copy.assignId(20L);
        copy.markBorrowed();
        Book book = Book.create("123", "Title", "Pub", 2020, "Loc", "Sum", List.of(), List.of(), now);
        book.assignId(100L);
        book.addCopy();
        book.borrowCopy();
        BorrowRecord record = BorrowRecord.restore(
                30L, 10L, 20L, now.minusSeconds(40L * 86400), LocalDate.now().minusDays(2),
                null, 0, BorrowStatus.BORROWING, now.minusSeconds(40L * 86400), now);
        BorrowRule rule = BorrowRule.create("DEFAULT", 30, 5, 1, BigDecimal.valueOf(0.5), now);

        when(bookRepository.findCopyByBarcodeForUpdate("B001")).thenReturn(Optional.of(copy));
        when(borrowRecordRepository.findActiveByBookCopyIdForUpdate(20L)).thenReturn(Optional.of(record));
        when(bookRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(book));
        when(borrowRuleRepository.findByReaderType("DEFAULT")).thenReturn(Optional.of(rule));

        borrowApplicationService.returnBook("B001");

        var fineCaptor = org.mockito.ArgumentCaptor.forClass(FineRecord.class);
        verify(fineRecordRepository).save(fineCaptor.capture());
        assertEquals(new BigDecimal("1.0"), fineCaptor.getValue().amount());
        assertEquals(FineStatus.UNPAID, fineCaptor.getValue().status());
        assertEquals(BorrowStatus.RETURNED, record.status());
        assertTrue(copy.isAvailable());
        assertEquals(1, book.availableCopies());
    }

    @Test
    void renewBook_OtherReadersRecord_IsForbidden() {
        Reader reader = Reader.create(1L, "R001", "John", "123", "a@b.com", LocalDate.now());
        reader.assignId(10L);
        BorrowRecord record = BorrowRecord.create(99L, 20L, 30, Instant.now());
        record.assignId(30L);
        when(readerRepository.findByUserAccountId(1L)).thenReturn(Optional.of(reader));
        when(borrowRecordRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(record));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowApplicationService.renewBook(1L, 30L));

        assertEquals(ResultCode.AUTH_FORBIDDEN, ex.resultCode());
        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    void borrowBook_HasOverdueRecords_ThrowsException() {
        Reader reader = Reader.create(1L, "R001", "John", "123", "a@b.com", LocalDate.now());
        reader.assignId(10L);
        when(readerRepository.findByReaderNo("R001")).thenReturn(Optional.of(reader));
        when(fineRecordRepository.hasUnpaidFines(10L)).thenReturn(false);
        when(borrowRecordRepository.hasOverdueRecords(10L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowApplicationService.borrowBook("R001", "B001"));

        assertEquals(ResultCode.READER_HAS_OVERDUE, ex.resultCode());
    }

    @Test
    void borrowBook_NoAvailableCopy_ThrowsException() {
        Reader reader = Reader.create(1L, "R001", "John", "123", "a@b.com", LocalDate.now());
        reader.assignId(10L);
        when(readerRepository.findByReaderNo("R001")).thenReturn(Optional.of(reader));
        when(fineRecordRepository.hasUnpaidFines(10L)).thenReturn(false);
        when(borrowRecordRepository.hasOverdueRecords(10L)).thenReturn(false);
        BorrowRule rule = BorrowRule.create("DEFAULT", 30, 5, 1, BigDecimal.valueOf(0.5), Instant.now());
        when(borrowRuleRepository.findByReaderType("DEFAULT")).thenReturn(Optional.of(rule));
        when(borrowRecordRepository.countUnreturnedByReaderId(10L)).thenReturn(0);

        BookCopy copy = BookCopy.create(100L, "B001", Instant.now());
        copy.assignId(20L);
        copy.markBorrowed();
        when(bookRepository.findCopyByBarcodeForUpdate("B001")).thenReturn(Optional.of(copy));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowApplicationService.borrowBook("R001", "B001"));

        assertEquals(ResultCode.BORROWING_NO_AVAILABLE_COPY, ex.resultCode());
    }

    @Test
    void borrowBook_ReaderNotFound_ThrowsException() {
        when(readerRepository.findByReaderNo("GHOST")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowApplicationService.borrowBook("GHOST", "B001"));

        assertEquals(ResultCode.READER_NOT_FOUND, ex.resultCode());
    }

    @Test
    void returnBook_Normal_NoFineCreated() {
        Instant now = Instant.now();
        BookCopy copy = BookCopy.create(100L, "B001", now);
        copy.assignId(20L);
        copy.markBorrowed();
        Book book = Book.create("123", "Title", "Pub", 2020, "Loc", "Sum", List.of(), List.of(), now);
        book.assignId(100L);
        book.addCopy();
        book.borrowCopy();

        BorrowRecord record = BorrowRecord.restore(
                30L, 10L, 20L, now.minusSeconds(5L * 86400), LocalDate.now().plusDays(25),
                null, 0, BorrowStatus.BORROWING, now.minusSeconds(5L * 86400), now);

        when(bookRepository.findCopyByBarcodeForUpdate("B001")).thenReturn(Optional.of(copy));
        when(borrowRecordRepository.findActiveByBookCopyIdForUpdate(20L)).thenReturn(Optional.of(record));
        when(bookRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(book));

        borrowApplicationService.returnBook("B001");

        verify(fineRecordRepository, never()).save(any());
        assertEquals(BorrowStatus.RETURNED, record.status());
        assertTrue(copy.isAvailable());
        assertEquals(1, book.availableCopies());
    }

    @Test
    void renewBook_AtRenewLimit_ThrowsException() {
        Reader reader = Reader.create(1L, "R001", "John", "123", "a@b.com", LocalDate.now());
        reader.assignId(10L);
        BorrowRecord record = BorrowRecord.restore(
                30L, 10L, 20L, Instant.now(), LocalDate.now().plusDays(30),
                null, 1, BorrowStatus.BORROWING, Instant.now(), Instant.now());
        record.assignId(30L);
        BorrowRule rule = BorrowRule.create("DEFAULT", 30, 5, 1, BigDecimal.valueOf(0.5), Instant.now());

        when(readerRepository.findByUserAccountId(1L)).thenReturn(Optional.of(reader));
        when(borrowRecordRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(record));
        when(borrowRuleRepository.findByReaderType("DEFAULT")).thenReturn(Optional.of(rule));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowApplicationService.renewBook(1L, 30L));

        assertEquals(ResultCode.BORROWING_RENEW_LIMIT_EXCEEDED, ex.resultCode());
    }

    @Test
    void renewBook_UnpaidFines_ThrowsException() {
        Reader reader = Reader.create(1L, "R001", "John", "123", "a@b.com", LocalDate.now());
        reader.assignId(10L);
        BorrowRecord record = BorrowRecord.create(10L, 20L, 30, Instant.now());
        record.assignId(30L);
        BorrowRule rule = BorrowRule.create("DEFAULT", 30, 5, 1, BigDecimal.valueOf(0.5), Instant.now());

        when(readerRepository.findByUserAccountId(1L)).thenReturn(Optional.of(reader));
        when(borrowRecordRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(record));
        when(borrowRuleRepository.findByReaderType("DEFAULT")).thenReturn(Optional.of(rule));
        when(fineRecordRepository.hasUnpaidFines(10L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowApplicationService.renewBook(1L, 30L));

        assertEquals(ResultCode.BORROWING_FINE_UNPAID, ex.resultCode());
    }

    @Test
    void renewBook_Success_ExtendsDueDateAndIncreasesCount() {
        Reader reader = Reader.create(1L, "R001", "John", "123", "a@b.com", LocalDate.now());
        reader.assignId(10L);
        LocalDate originalDue = LocalDate.now().plusDays(30);
        BorrowRecord record = BorrowRecord.restore(
                30L, 10L, 20L, Instant.now(), originalDue,
                null, 0, BorrowStatus.BORROWING, Instant.now(), Instant.now());
        record.assignId(30L);
        BorrowRule rule = BorrowRule.create("DEFAULT", 30, 5, 1, BigDecimal.valueOf(0.5), Instant.now());

        when(readerRepository.findByUserAccountId(1L)).thenReturn(Optional.of(reader));
        when(borrowRecordRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(record));
        when(borrowRuleRepository.findByReaderType("DEFAULT")).thenReturn(Optional.of(rule));
        when(fineRecordRepository.hasUnpaidFines(10L)).thenReturn(false);

        borrowApplicationService.renewBook(1L, 30L);

        assertEquals(1, record.renewCount());
        assertEquals(originalDue.plusDays(30), record.dueDate());
        verify(borrowRecordRepository).save(record);
    }
}
