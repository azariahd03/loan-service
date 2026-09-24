package com.example.loanservice.service.imp;

import com.example.loanservice.Client.AccountClient;
import com.example.loanservice.Dto.AccountResponse;
import com.example.loanservice.Dto.LoanRequest;
import com.example.loanservice.Dto.LoanResponse;
import com.example.loanservice.Exception.*;
import com.example.loanservice.entity.Loan;
import com.example.loanservice.entity.LoanStatus;
import com.example.loanservice.repository.LoanRepository;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class LoanServiceImp implements LoanService{

    private final AccountClient accountClient;

    private final LoanRepository loanRepository;

    public LoanServiceImp(LoanRepository loanRepository,
                          AccountClient accountClient) {
        this.loanRepository = loanRepository;
        this.accountClient = accountClient;
    }

    @Override
    @Transactional
    public LoanResponse createLoan(LoanRequest loanRequest) {

        if (loanRequest.getAmount() <= 0) {
            throw new InvalidLoanAmountException("Loan amount must be greater than zero");
        }
        AccountResponse account;
        try {
            account = accountClient.getAccountById(
                    loanRequest.getAccountId()
            );
        } catch (FeignException.NotFound ex) {
            throw new AccountNotFoundException(
                    "Account doesn't exist"
            );
        } catch (RetryableException ex) {
            throw new AccountServiceUnavailableException(
                    "Account service is currently unavailable"
            );
        }

        boolean activeLoanExists =
                loanRepository.existsByAccountIdAndStatus(
                        loanRequest.getAccountId(),
                        LoanStatus.ACTIVE
                );

        if (activeLoanExists) {
            throw new ActiveLoanExistsException("Active loan already exists for this account");
        }
        if (account.getBalance() <
                loanRequest.getAmount() * (2.0 / 3.0)) {

            throw new InsufficientBalanceException("Account balance is not sufficient for this loan");
        }

        Loan loan = new Loan();
        loan.setAccountId(loanRequest.getAccountId());
        loan.setAmount(loanRequest.getAmount());
        loan.setStatus(LoanStatus.ACTIVE);

        Loan savedLoan = loanRepository.save(loan);

        accountClient.deposit(
                loanRequest.getAccountId(),
                Map.of("amount", loanRequest.getAmount())
        );

        return new LoanResponse(
                savedLoan.getId(),
                savedLoan.getAccountId(),
                savedLoan.getAmount(),
                savedLoan.getStatus()
        );
    }

    @Override
    public LoanResponse repayLoan(Long accountId) {

        Loan loan = loanRepository
                .findByAccountIdAndStatus(
                        accountId,
                        LoanStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new NoActiveLoanException("No active loan found"));

        accountClient.loanRepayment(accountId, Map.of("amount", loan.getAmount()));

        loan.setStatus(LoanStatus.CLOSED);

        Loan savedLoan = loanRepository.save(loan);

        return new LoanResponse(
                savedLoan.getId(),
                savedLoan.getAccountId(),
                savedLoan.getAmount(),
                savedLoan.getStatus()
        );
    }
    @Override
    public Page<LoanResponse> getLoanHistory(Long accountId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Loan> loans = loanRepository.findByAccountIdOrderByIdDesc(accountId, pageable);

        return loans.map(loan -> new LoanResponse(
                        loan.getId(),
                        loan.getAccountId(),
                        loan.getAmount(),
                        loan.getStatus()
                )
        );
    }
    @Override
    public LoanResponse getActiveLoan(Long accountId) {

        Loan loan = loanRepository
                .findByAccountIdAndStatus(accountId, LoanStatus.ACTIVE)
                .orElseThrow(() -> new NoActiveLoanException("No active loan found"));

        return new LoanResponse(
                loan.getId(),
                loan.getAccountId(),
                loan.getAmount(),
                loan.getStatus()
        );
    }
}
