package com.library.application.fine;

import com.library.domain.fine.FineRecord;
import com.library.domain.fine.FineRecordRepository;
import com.library.domain.fine.FineStatus;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FineApplicationServiceTest {

    @Mock
    private FineRecordRepository fineRecordRepository;

    private FineApplicationService fineApplicationService;

    @BeforeEach
    void setUp() {
        fineApplicationService = new FineApplicationService(fineRecordRepository);
    }

    @Test
    void payFine_Success() {
        Long fineId = 1L;
        BigDecimal amount = BigDecimal.valueOf(5.00);

        FineRecord fine = FineRecord.create(10L, 20L, amount, "Overdue", Instant.now());
        fine.assignId(fineId);

        when(fineRecordRepository.findByIdForUpdate(fineId)).thenReturn(Optional.of(fine));

        fineApplicationService.payFine(fineId, amount);

        assertEquals(FineStatus.PAID, fine.status());
        assertNotNull(fine.paidAt());
        verify(fineRecordRepository).save(fine);
    }

    @Test
    void payFine_AmountMismatch_ThrowsException() {
        Long fineId = 1L;
        BigDecimal amount = BigDecimal.valueOf(5.00);

        FineRecord fine = FineRecord.create(10L, 20L, amount, "Overdue", Instant.now());
        fine.assignId(fineId);

        when(fineRecordRepository.findByIdForUpdate(fineId)).thenReturn(Optional.of(fine));

        BusinessException ex = assertThrows(BusinessException.class, () -> fineApplicationService.payFine(fineId, BigDecimal.valueOf(4.00)));
        assertEquals(ResultCode.FINE_AMOUNT_MISMATCH, ex.resultCode());
        verify(fineRecordRepository, never()).save(any());
    }

    @Test
    void payFine_AlreadyPaid_ThrowsException() {
        Long fineId = 1L;
        BigDecimal amount = BigDecimal.valueOf(5.00);

        FineRecord fine = FineRecord.create(10L, 20L, amount, "Overdue", Instant.now());
        fine.assignId(fineId);
        fine.pay(amount, Instant.now());

        when(fineRecordRepository.findByIdForUpdate(fineId)).thenReturn(Optional.of(fine));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> fineApplicationService.payFine(fineId, amount));
        assertEquals(ResultCode.PARAM_INVALID, ex.resultCode());
        verify(fineRecordRepository, never()).save(any());
    }

    @Test
    void payFine_NotFound_ThrowsException() {
        when(fineRecordRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> fineApplicationService.payFine(999L, BigDecimal.valueOf(5.00)));
        assertEquals(ResultCode.PARAM_INVALID, ex.resultCode());
    }
}
