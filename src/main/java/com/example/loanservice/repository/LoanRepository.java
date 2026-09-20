package com.example.loanservice.repository;

import com.example.loanservice.entity.Loan;
import com.example.loanservice.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan,Long> {
    boolean existsByAccountIdAndStatus(Long accountId, LoanStatus status);

    Optional<Loan> findByAccountIdAndStatus(Long accountId, LoanStatus status);
}
