package com.library.interfaces.rest;

import com.library.application.fine.FineApplicationService;
import com.library.domain.shared.Result;
import com.library.interfaces.dto.fine.PayFineRequest;
import com.library.interfaces.dto.fine.FineResponse;
import com.library.interfaces.dto.catalog.PageData;
import com.library.domain.fine.FineRecordRepository;
import com.library.domain.fine.FineStatus;
import com.library.interfaces.security.RequireLibrarian;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fines")
@RequireLibrarian
public class FineController {

    private final FineApplicationService fineAppService;
    private final FineRecordRepository fineRecordRepository;

    public FineController(FineApplicationService fineAppService, FineRecordRepository fineRecordRepository) {
        this.fineAppService = fineAppService;
        this.fineRecordRepository = fineRecordRepository;
    }

    @org.springframework.web.bind.annotation.GetMapping
    public Result<PageData<FineResponse>> getFines(
            @org.springframework.web.bind.annotation.RequestParam(required = false) FineStatus status,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        var fines = fineRecordRepository.findAll().stream()
                .filter(fine -> status == null || fine.status() == status)
                .map(fine -> new FineResponse(
                        fine.id(), fine.borrowRecordId(), fine.readerId(), fine.amount(), fine.reason(),
                        fine.status(), fine.createdAt(), fine.paidAt()))
                .toList();
        return Result.ok(PageData.from(fines, page, size));
    }

    @PutMapping("/{id}/pay")
    public Result<Void> payFine(@PathVariable Long id, @Valid @RequestBody PayFineRequest request) {
        fineAppService.payFine(id, request.amount());
        return Result.ok(null);
    }
}
