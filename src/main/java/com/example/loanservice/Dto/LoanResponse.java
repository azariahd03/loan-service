package com.example.loanservice.Dto;

import com.example.loanservice.entity.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class LoanResponse {
    private Long id;
    private Long accountId;
    private double amount;
    private LoanStatus status;
}
