package com.example.loanservice.Exception;

public class NoActiveLoanException extends RuntimeException{
    public NoActiveLoanException(String message){
        super(message);
    }
}
