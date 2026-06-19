package com.library.interfaces.rest;

import com.library.domain.borrow.BorrowRule;
import com.library.domain.borrow.BorrowRuleRepository;
import com.library.domain.shared.Result;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import com.library.interfaces.dto.borrow.RuleRequest;
import com.library.interfaces.dto.borrow.RuleResponse;
import com.library.interfaces.security.RequireLibrarian;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rules")
@RequireLibrarian
public class RuleController {

    private final BorrowRuleRepository borrowRuleRepository;

    public RuleController(BorrowRuleRepository borrowRuleRepository) {
        this.borrowRuleRepository = borrowRuleRepository;
    }

    @GetMapping
    public Result<List<RuleResponse>> getRules() {
        List<RuleResponse> responses = borrowRuleRepository.findAll().stream()
                .map(rule -> new RuleResponse(
                        rule.id(),
                        rule.readerType(),
                        rule.maxBorrowDays(),
                        rule.maxBorrowCount(),
                        rule.maxRenewCount(),
                        rule.finePerDay()
                ))
                .collect(Collectors.toList());
        return Result.ok(responses);
    }

    @PutMapping("/{id}")
    public Result<Void> updateRule(@PathVariable Long id, @Valid @RequestBody RuleRequest request) {
        BorrowRule rule = borrowRuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.PARAM_INVALID, "Rule not found"));

        rule.updateInfo(
                request.maxBorrowDays(),
                request.maxBorrowCount(),
                request.maxRenewCount(),
                request.finePerDay(),
                Instant.now()
        );
        borrowRuleRepository.save(rule);
        return Result.ok(null);
    }
}
