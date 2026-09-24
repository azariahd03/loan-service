package com.example.loanservice.Exception;

public class AccountServiceUnavailableException extends RuntimeException{
    public AccountServiceUnavailableException(String message) {
        super(message);
    }
}
