package com.library.interfaces.rest;

import com.library.application.borrow.BorrowApplicationService;
import com.library.domain.shared.Result;
import com.library.interfaces.security.RequireReader;
import com.library.domain.borrow.BorrowRecordRepository;
import com.library.domain.reader.ReaderRepository;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import com.library.interfaces.dto.borrow.BorrowResponse;
import com.library.interfaces.dto.catalog.PageData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/my/borrows")
@RequireReader
public class MyBorrowController {

    private final BorrowApplicationService borrowAppService;
    private final BorrowRecordRepository borrowRecordRepository;
    private final ReaderRepository readerRepository;

    public MyBorrowController(BorrowApplicationService borrowAppService,
                              BorrowRecordRepository borrowRecordRepository,
                              ReaderRepository readerRepository) {
        this.borrowAppService = borrowAppService;
        this.borrowRecordRepository = borrowRecordRepository;
        this.readerRepository = readerRepository;
    }

    @GetMapping
    public Result<PageData<BorrowResponse>> getMyBorrows(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        var reader = readerRepository.findByUserAccountId(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.READER_NOT_FOUND));
        var records = borrowRecordRepository.findByReaderId(reader.id()).stream()
                .map(record -> new BorrowResponse(
                        record.id(), record.readerId(), record.bookCopyId(), record.borrowTime(),
                        record.dueDate(), record.returnTime(), record.renewCount(), record.status()))
                .toList();
        return Result.ok(PageData.from(records, page, size));
    }

    @PutMapping("/{id}/renew")
    public Result<Void> renewBook(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        borrowAppService.renewBook(userId, id);
        return Result.ok(null);
    }
}
