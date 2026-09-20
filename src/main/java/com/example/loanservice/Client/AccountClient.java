package com.example.loanservice.Client;

import com.example.loanservice.Dto.AccountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "account-service",
        url = "${account.service.url}"
)
public interface AccountClient {

    @GetMapping("/api/accounts/{id}")
    AccountResponse getAccountById(@PathVariable("id") Long id);

    @PutMapping("/api/accounts/{id}/deposit")
    AccountResponse deposit(@PathVariable("id") Long id, @RequestBody Map<String, Double> request);

    @PutMapping("/api/accounts/{id}/loan-repayment")
    AccountResponse loanRepayment(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Double> request
    );
}
