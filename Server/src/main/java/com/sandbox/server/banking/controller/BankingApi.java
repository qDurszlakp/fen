package com.sandbox.server.banking.controller;

import com.sandbox.server.banking.dto.*;
import com.sandbox.server.banking.service.BankingService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class BankingApi {

    public final BankingService bankingService;

    @GetMapping("/accounts")
    public ResponseEntity<PagedModel<AccountDto>> accounts(
            @PageableDefault(size = 20, sort = "accountNumber") Pageable pageable) {

        return ResponseEntity.ok(new PagedModel<>(bankingService.getAccounts(pageable)));
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody CreateAccountDto account) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bankingService.createAccount(account));
    }

    @GetMapping("/cards")
    public ResponseEntity<PagedModel<CardDto>> cards(
            @PageableDefault(size = 20, sort = "cardNumber") Pageable pageable) {

        return ResponseEntity.ok(new PagedModel<>(bankingService.getCards(pageable)));
    }

    @PostMapping("/countries")
    public ResponseEntity<CountryDto> createCountry(@Valid @RequestBody CreateCountryDto country) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bankingService.createCountry(country));
    }

}
