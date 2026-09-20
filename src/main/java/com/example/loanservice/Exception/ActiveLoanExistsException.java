package com.example.loanservice.Exception;

public class ActiveLoanExistsException extends RuntimeException{
    public ActiveLoanExistsException(String message) {
        super(message);
    }
}
