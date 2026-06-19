package com.library.interfaces.rest;

import com.library.application.borrow.BorrowApplicationService;
import com.library.domain.shared.Result;
import com.library.interfaces.dto.borrow.BorrowRequest;
import com.library.interfaces.dto.borrow.BorrowResponse;
import com.library.interfaces.dto.catalog.PageData;
import com.library.domain.borrow.BorrowRecordRepository;
import com.library.domain.borrow.BorrowStatus;
import com.library.interfaces.security.RequireLibrarian;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/borrows")
@RequireLibrarian
public class BorrowController {

    private final BorrowApplicationService borrowAppService;
    private final BorrowRecordRepository borrowRecordRepository;

    public BorrowController(BorrowApplicationService borrowAppService,
                            BorrowRecordRepository borrowRecordRepository) {
        this.borrowAppService = borrowAppService;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    @org.springframework.web.bind.annotation.GetMapping
    public Result<PageData<BorrowResponse>> getBorrows(
            @org.springframework.web.bind.annotation.RequestParam(required = false) BorrowStatus status,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        var records = borrowRecordRepository.findAll().stream()
                .filter(record -> status == null || record.status() == status)
                .map(record -> new BorrowResponse(
                        record.id(), record.readerId(), record.bookCopyId(), record.borrowTime(),
                        record.dueDate(), record.returnTime(), record.renewCount(), record.status()))
                .toList();
        return Result.ok(PageData.from(records, page, size));
    }

    @PostMapping
    public Result<Long> borrowBook(@Valid @RequestBody BorrowRequest request) {
        Long id = borrowAppService.borrowBook(request.readerNo(), request.barcode());
        return Result.ok(id);
    }

    @PutMapping("/return/{barcode}")
    public Result<Void> returnBook(@PathVariable String barcode) {
        borrowAppService.returnBook(barcode);
        return Result.ok(null);
    }
}
