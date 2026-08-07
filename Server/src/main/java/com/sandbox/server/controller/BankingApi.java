package com.sandbox.server.controller;

import com.sandbox.server.dto.*;
import com.sandbox.server.service.DbService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class BankingApi {

    public final DbService dbService;

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> accounts() {
        return ResponseEntity.ok(dbService.getAccounts());
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody CreateAccountDto account) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dbService.createAccount(account));
    }

    @GetMapping("/cards")
    public ResponseEntity<List<CardDto>> cards() {
        return ResponseEntity.ok(dbService.getCards());
    }

    @PostMapping("/countries")
    public ResponseEntity<CountryDto> createCountry(@Valid @RequestBody CreateCountryDto country) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dbService.createCountry(country));
    }

}
