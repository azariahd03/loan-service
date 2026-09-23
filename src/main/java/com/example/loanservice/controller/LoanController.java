package com.example.loanservice.controller;

import com.example.loanservice.Dto.LoanRequest;
import com.example.loanservice.Dto.LoanResponse;
import com.example.loanservice.service.imp.LoanService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(@RequestBody LoanRequest loanRequest) {

        LoanResponse response = loanService.createLoan(loanRequest);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{accountId}/repay")
    public ResponseEntity<LoanResponse> repayLoan(@PathVariable Long accountId) {

        LoanResponse response = loanService.repayLoan(accountId);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/account/{accountId}")
    public ResponseEntity<Page<LoanResponse>> getLoanHistory(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<LoanResponse> history = loanService.getLoanHistory(accountId, page, size);
        return ResponseEntity.ok(history);
    }
}
