package com.example.loanservice.Controller;

import com.example.loanservice.Dto.LoanRequest;
import com.example.loanservice.Dto.LoanResponse;
import com.example.loanservice.Exception.ActiveLoanExistsException;
import com.example.loanservice.Exception.GlobalExceptionHandler;
import com.example.loanservice.Exception.InvalidLoanAmountException;
import com.example.loanservice.Exception.NoActiveLoanException;
import com.example.loanservice.controller.LoanController;
import com.example.loanservice.entity.LoanStatus;
import com.example.loanservice.service.imp.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class LoanControllerTest {
    @Mock
    private LoanService loanService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {

        MockitoAnnotations.openMocks(this);

        LoanController loanController =
                new LoanController(loanService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(loanController)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();
    }
    @Test
    void createLoan_shouldReturnLoan_whenRequestIsValid() throws Exception {

        LoanResponse response =
                new LoanResponse(
                        1L,
                        10L,
                        1000,
                        LoanStatus.ACTIVE
                );

        when(loanService.createLoan(any(LoanRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "accountId": 10,
                              "amount": 1000
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountId").value(10))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
    @Test
    void repayLoan_shouldReturnClosedLoan_whenRepaymentIsSuccessful() throws Exception {

        LoanResponse response =
                new LoanResponse(
                        1L,
                        10L,
                        1000,
                        LoanStatus.CLOSED
                );

        when(loanService.repayLoan(10L))
                .thenReturn(response);

        mockMvc.perform(put("/api/loans/10/repay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountId").value(10))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }
    @Test
    void createLoan_shouldReturnBadRequest_whenAmountIsInvalid() throws Exception {

        when(loanService.createLoan(any(LoanRequest.class)))
                .thenThrow(
                        new InvalidLoanAmountException(
                                "Loan amount must be greater than zero"
                        )
                );

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "accountId": 10,
                              "amount": 0
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Loan amount must be greater than zero"));
    }
    @Test
    void createLoan_shouldReturnBadRequest_whenActiveLoanAlreadyExists() throws Exception {

        when(loanService.createLoan(any(LoanRequest.class)))
                .thenThrow(
                        new ActiveLoanExistsException(
                                "Active loan already exists for this account"
                        )
                );

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "accountId": 10,
                              "amount": 1000
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Active loan already exists for this account"));
    }
    @Test
    void repayLoan_shouldReturnNotFound_whenNoActiveLoanExists() throws Exception {

        when(loanService.repayLoan(10L))
                .thenThrow(
                        new NoActiveLoanException(
                                "No active loan found"
                        )
                );

        mockMvc.perform(put("/api/loans/10/repay"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("No active loan found"));
    }
    @Test
    void getLoanHistory_shouldReturnLoanHistory() throws Exception {

        LoanResponse loanResponse =
                new LoanResponse(
                        6L,
                        11L,
                        1000,
                        LoanStatus.CLOSED
                );

        Page<LoanResponse> page =
                new PageImpl<>(
                        List.of(loanResponse),
                        PageRequest.of(0, 10),
                        1
                );

        when(loanService.getLoanHistory(11L, 0, 10))
                .thenReturn(page);

        mockMvc.perform(get("/api/loans/account/11")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(6))
                .andExpect(jsonPath("$.content[0].accountId").value(11))
                .andExpect(jsonPath("$.content[0].amount").value(1000))
                .andExpect(jsonPath("$.content[0].status").value("CLOSED"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    @Test
    void getActiveLoan_shouldReturnActiveLoan() throws Exception {

        LoanResponse response =
                new LoanResponse(
                        7L,
                        11L,
                        500,
                        LoanStatus.ACTIVE
                );

        when(loanService.getActiveLoan(11L))
                .thenReturn(response);

        mockMvc.perform(get("/api/loans/account/11/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.accountId").value(11))
                .andExpect(jsonPath("$.amount").value(500))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getActiveLoan_shouldReturnNotFound_whenNoActiveLoanExists() throws Exception {

        when(loanService.getActiveLoan(11L))
                .thenThrow(
                        new NoActiveLoanException(
                                "No active loan found"
                        )
                );

        mockMvc.perform(get("/api/loans/account/11/active"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("No active loan found"));
    }
}
