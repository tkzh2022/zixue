package com.library.application.dashboard;

import com.library.domain.book.BookRepository;
import com.library.domain.borrow.BorrowRecordRepository;
import com.library.domain.fine.FineRecordRepository;
import com.library.domain.reader.Reader;
import com.library.domain.reader.ReaderRepository;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import com.library.domain.borrow.BorrowStatus;
import com.library.domain.fine.FineStatus;

@Service
public class DashboardApplicationService {

    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final FineRecordRepository fineRecordRepository;

    public DashboardApplicationService(BookRepository bookRepository,
                                       ReaderRepository readerRepository,
                                       BorrowRecordRepository borrowRecordRepository,
                                       FineRecordRepository fineRecordRepository) {
        this.bookRepository = bookRepository;
        this.readerRepository = readerRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.fineRecordRepository = fineRecordRepository;
    }

    public AdminDashboardView getAdminStats() {
        long totalBooks = bookRepository.findAll().size();
        long totalReaders = readerRepository.findAll().size();
        long activeBorrows = borrowRecordRepository.findAll().stream()
                .filter(record -> record.status() == BorrowStatus.BORROWING || record.status() == BorrowStatus.OVERDUE)
                .count();
        BigDecimal unpaidFines = fineRecordRepository.findAll().stream()
                .filter(fine -> fine.status() == FineStatus.UNPAID)
                .map(fine -> fine.amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new AdminDashboardView(totalBooks, totalReaders, activeBorrows, unpaidFines);
    }

    public ReaderDashboardView getReaderStats(Long userAccountId) {
        Reader reader = readerRepository.findByUserAccountId(userAccountId)
                .orElseThrow(() -> new BusinessException(ResultCode.READER_NOT_FOUND));

        var borrows = borrowRecordRepository.findByReaderId(reader.id());
        int activeBorrows = (int) borrows.stream()
                .filter(record -> record.status() == BorrowStatus.BORROWING || record.status() == BorrowStatus.OVERDUE)
                .count();
        int overdueBorrows = (int) borrows.stream()
                .filter(record -> record.status() == BorrowStatus.OVERDUE || record.isOverdue(java.time.LocalDate.now()))
                .count();
        BigDecimal unpaidFines = fineRecordRepository.findByReaderId(reader.id()).stream()
                .filter(fine -> fine.status() == FineStatus.UNPAID)
                .map(fine -> fine.amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ReaderDashboardView(activeBorrows, overdueBorrows, unpaidFines);
    }
}
