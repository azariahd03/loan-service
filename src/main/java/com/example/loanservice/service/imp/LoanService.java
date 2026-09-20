package com.example.loanservice.service.imp;

import com.example.loanservice.Dto.LoanRequest;
import com.example.loanservice.Dto.LoanResponse;

public interface LoanService {

    LoanResponse createLoan (LoanRequest loanRequest);

    LoanResponse repayLoan (Long accountId);
}