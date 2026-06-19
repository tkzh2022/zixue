package com.library.interfaces.rest;

import com.library.domain.shared.Result;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import com.library.domain.reader.ReaderRepository;
import com.library.domain.fine.FineRecordRepository;
import com.library.interfaces.dto.fine.FineResponse;
import com.library.interfaces.dto.catalog.PageData;
import com.library.interfaces.security.RequireReader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/my/fines")
@RequireReader
public class MyFineController {

    private final ReaderRepository readerRepository;
    private final FineRecordRepository fineRecordRepository;

    public MyFineController(ReaderRepository readerRepository, FineRecordRepository fineRecordRepository) {
        this.readerRepository = readerRepository;
        this.fineRecordRepository = fineRecordRepository;
    }

    @GetMapping
    public Result<PageData<FineResponse>> getMyFines(
            @org.springframework.web.bind.annotation.RequestAttribute("userId") Long userId,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        var reader = readerRepository.findByUserAccountId(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.READER_NOT_FOUND));
        var fines = fineRecordRepository.findByReaderId(reader.id()).stream()
                .map(fine -> new FineResponse(
                        fine.id(), fine.borrowRecordId(), fine.readerId(), fine.amount(), fine.reason(),
                        fine.status(), fine.createdAt(), fine.paidAt()))
                .toList();
        return Result.ok(PageData.from(fines, page, size));
    }
}
