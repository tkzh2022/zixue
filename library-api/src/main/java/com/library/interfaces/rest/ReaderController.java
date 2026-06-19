package com.library.interfaces.rest;

import com.library.application.reader.ReaderApplicationService;
import com.library.application.reader.command.CreateReaderCommand;
import com.library.domain.reader.Reader;
import com.library.domain.reader.ReaderRepository;
import com.library.domain.reader.ReaderStatus;
import com.library.domain.shared.Result;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import com.library.interfaces.dto.reader.ReaderRequest;
import com.library.interfaces.dto.reader.ReaderResponse;
import com.library.interfaces.dto.reader.ReaderStatusRequest;
import com.library.interfaces.dto.reader.ReaderUpdateRequest;
import com.library.interfaces.dto.catalog.PageData;
import com.library.interfaces.security.RequireLibrarian;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/readers")
@RequireLibrarian
public class ReaderController {

    private final ReaderApplicationService readerAppService;
    private final ReaderRepository readerRepository;

    public ReaderController(ReaderApplicationService readerAppService, ReaderRepository readerRepository) {
        this.readerAppService = readerAppService;
        this.readerRepository = readerRepository;
    }

    @GetMapping
    public Result<PageData<ReaderResponse>> getReaders(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword,
            @org.springframework.web.bind.annotation.RequestParam(required = false) ReaderStatus status,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        var readers = readerRepository.findAll().stream()
                .filter(reader -> normalized.isEmpty()
                        || reader.readerNo().toLowerCase(Locale.ROOT).contains(normalized)
                        || reader.name().toLowerCase(Locale.ROOT).contains(normalized))
                .filter(reader -> status == null || reader.status() == status)
                .map(this::toResponse)
                .toList();
        return Result.ok(PageData.from(readers, page, size));
    }

    @PostMapping
    public Result<Long> createReader(@Valid @RequestBody ReaderRequest request) {
        CreateReaderCommand cmd = new CreateReaderCommand(
                request.userAccountId(),
                request.readerNo(),
                request.name(),
                request.phone(),
                request.email(),
                request.registerDate()
        );
        Long id = readerAppService.createReader(cmd);
        return Result.ok(id);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody ReaderStatusRequest request) {
        ReaderStatus status = ReaderStatus.valueOf(request.status());
        readerAppService.updateStatus(id, status);
        return Result.ok(null);
    }

    @PutMapping("/{id}")
    public Result<Void> updateReader(@PathVariable Long id, @Valid @RequestBody ReaderUpdateRequest request) {
        readerAppService.updateReader(new com.library.application.reader.command.UpdateReaderCommand(
                id, request.name(), request.phone(), request.email()));
        return Result.ok(null);
    }

    @GetMapping("/{id}")
    public Result<ReaderResponse> getReader(@PathVariable Long id) {
        Reader reader = readerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.PARAM_INVALID, "Reader not found"));

        return Result.ok(toResponse(reader));
    }

    private ReaderResponse toResponse(Reader reader) {
        return new ReaderResponse(
                reader.id(),
                reader.userAccountId(),
                reader.readerNo(),
                reader.name(),
                reader.phone(),
                reader.email(),
                reader.status(),
                reader.registerDate()
        );
    }
}
