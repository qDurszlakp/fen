package com.sandbox.server.banking.controller;

import com.sandbox.server.banking.dto.*;
import com.sandbox.server.banking.service.BankingService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class BankingApi {

    public final BankingService bankingService;

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> accounts() {
        return ResponseEntity.ok(bankingService.getAccounts());
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody CreateAccountDto account) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bankingService.createAccount(account));
    }

    @GetMapping("/cards")
    public ResponseEntity<List<CardDto>> cards() {
        return ResponseEntity.ok(bankingService.getCards());
    }

    @PostMapping("/countries")
    public ResponseEntity<CountryDto> createCountry(@Valid @RequestBody CreateCountryDto country) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bankingService.createCountry(country));
    }

}
