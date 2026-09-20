package com.example.loanservice;

import com.example.loanservice.Client.AccountClient;
import com.example.loanservice.Dto.AccountResponse;
import com.example.loanservice.Dto.LoanRequest;
import com.example.loanservice.Dto.LoanResponse;
import com.example.loanservice.Exception.ActiveLoanExistsException;
import com.example.loanservice.Exception.InsufficientBalanceException;
import com.example.loanservice.Exception.InvalidLoanAmountException;
import com.example.loanservice.Exception.NoActiveLoanException;
import com.example.loanservice.entity.Loan;
import com.example.loanservice.entity.LoanStatus;
import com.example.loanservice.repository.LoanRepository;
import com.example.loanservice.service.imp.LoanServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class LoanServiceImpTest {
    @Mock
    private LoanRepository loanRepository;

    @Mock
    private AccountClient accountClient;

    private LoanServiceImp loanService;

    @BeforeEach
    void setup() {

        MockitoAnnotations.openMocks(this);

        loanService = new LoanServiceImp(
                loanRepository,
                accountClient
        );
    }
    @Test
    void createLoan_shouldCreateLoan_whenRequestIsValid() {

        LoanRequest loanRequest =
                new LoanRequest(10L, 1000);

        AccountResponse accountResponse =
                new AccountResponse(
                        10L,
                        "Test User",
                        5000
                );

        when(accountClient.getAccountById(10L))
                .thenReturn(accountResponse);

        when(loanRepository.existsByAccountIdAndStatus(
                10L,
                LoanStatus.ACTIVE
        )).thenReturn(false);

        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> {
                    Loan loan = invocation.getArgument(0);
                    loan.setId(1L);
                    return loan;
                });

        when(accountClient.deposit(
                any(Long.class),
                any()
        )).thenReturn(
                new AccountResponse(
                        10L,
                        "Test User",
                        6000
                )
        );

        LoanResponse result =
                loanService.createLoan(loanRequest);

        assertEquals(1L, result.getId());
        assertEquals(10L, result.getAccountId());
        assertEquals(1000, result.getAmount());
        assertEquals(
                LoanStatus.ACTIVE,
                result.getStatus()
        );
    }
    @Test
    void createLoan_shouldThrowException_whenAmountIsInvalid() {

        LoanRequest loanRequest =
                new LoanRequest(10L, 0);

        assertThrows(
                InvalidLoanAmountException.class,
                () -> loanService.createLoan(loanRequest)
        );
    }
    @Test
    void createLoan_shouldThrowException_whenActiveLoanAlreadyExists() {

        LoanRequest loanRequest =
                new LoanRequest(10L, 1000);

        AccountResponse accountResponse =
                new AccountResponse(
                        10L,
                        "Test User",
                        5000
                );

        when(accountClient.getAccountById(10L))
                .thenReturn(accountResponse);

        when(loanRepository.existsByAccountIdAndStatus(
                10L,
                LoanStatus.ACTIVE
        )).thenReturn(true);

        assertThrows(
                ActiveLoanExistsException.class,
                () -> loanService.createLoan(loanRequest)
        );
    }
    @Test
    void createLoan_shouldThrowException_whenBalanceIsInsufficient() {

        LoanRequest loanRequest =
                new LoanRequest(10L, 10000);

        AccountResponse accountResponse =
                new AccountResponse(
                        10L,
                        "Test User",
                        1000
                );

        when(accountClient.getAccountById(10L))
                .thenReturn(accountResponse);

        when(loanRepository.existsByAccountIdAndStatus(
                10L,
                LoanStatus.ACTIVE
        )).thenReturn(false);

        assertThrows(
                InsufficientBalanceException.class,
                () -> loanService.createLoan(loanRequest)
        );
    }
    @Test
    void repayLoan_shouldCloseLoan_whenRepaymentIsSuccessful() {

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setAccountId(10L);
        loan.setAmount(1000);
        loan.setStatus(LoanStatus.ACTIVE);

        when(loanRepository.findByAccountIdAndStatus(
                10L,
                LoanStatus.ACTIVE
        )).thenReturn(Optional.of(loan));

        when(accountClient.loanRepayment(
                any(Long.class),
                any()
        )).thenReturn(
                new AccountResponse(
                        10L,
                        "Test User",
                        4000
                )
        );

        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponse result =
                loanService.repayLoan(10L);

        assertEquals(
                LoanStatus.CLOSED,
                result.getStatus()
        );

        assertEquals(
                LoanStatus.CLOSED,
                loan.getStatus()
        );
    }
    @Test
    void repayLoan_shouldThrowException_whenNoActiveLoanExists() {

        when(loanRepository.findByAccountIdAndStatus(
                10L,
                LoanStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThrows(
                NoActiveLoanException.class,
                () -> loanService.repayLoan(10L)
        );
    }

}
