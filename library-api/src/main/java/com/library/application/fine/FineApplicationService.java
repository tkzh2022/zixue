package com.library.application.fine;

import com.library.domain.fine.FineRecord;
import com.library.domain.fine.FineRecordRepository;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class FineApplicationService {

    private final FineRecordRepository fineRecordRepository;

    public FineApplicationService(FineRecordRepository fineRecordRepository) {
        this.fineRecordRepository = fineRecordRepository;
    }

    @Transactional
    public void payFine(Long fineRecordId, BigDecimal amount) {
        FineRecord fine = fineRecordRepository.findByIdForUpdate(fineRecordId)
                .orElseThrow(() -> new BusinessException(ResultCode.PARAM_INVALID, "Fine record not found"));

        try {
            fine.pay(amount, Instant.now());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ResultCode.FINE_AMOUNT_MISMATCH, e.getMessage());
        } catch (IllegalStateException e) {
            throw new BusinessException(ResultCode.PARAM_INVALID, e.getMessage());
        }

        fineRecordRepository.save(fine);
    }
}
