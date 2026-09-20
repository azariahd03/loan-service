package com.example.loanservice.Exception;

public class InvalidLoanAmountException extends RuntimeException{

    public InvalidLoanAmountException(String message) {
        super(message);
    }
}
