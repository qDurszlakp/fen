package com.sandbox.server.controller;

import com.sandbox.server.dto.*;
import com.sandbox.server.service.DbService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/app")
public class DbApi {

    public final DbService dbService;

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountDto>> accounts() {
        return ResponseEntity.ok(dbService.getAccounts());
    }

    @GetMapping("/cards")
    public ResponseEntity<List<CardDto>> cards() {
        return ResponseEntity.ok(dbService.getCards());
    }

    @PostMapping("/country")
    public ResponseEntity<CountryDto> createCountry(@RequestBody CreateCountryDto country) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dbService.createCountry(country));
    }

    @PostMapping("/account")
    public ResponseEntity<AccountDto> createAccount(@RequestBody CreateAccountDto account) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dbService.createAccount(account));
    }

}
