package com.example.loanservice.service.imp;

import com.example.loanservice.Dto.LoanRequest;
import com.example.loanservice.Dto.LoanResponse;
import org.springframework.data.domain.Page;

public interface LoanService {

    LoanResponse createLoan (LoanRequest loanRequest);

    LoanResponse repayLoan (Long accountId);

    Page<LoanResponse> getLoanHistory(Long accountId, int page, int size);

    LoanResponse getActiveLoan(Long accountId);
}