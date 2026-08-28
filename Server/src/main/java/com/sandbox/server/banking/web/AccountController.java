package com.sandbox.server.banking.web;

import com.sandbox.server.banking.application.AccountApplicationService;
import com.sandbox.server.banking.web.dto.AccountDto;
import com.sandbox.server.banking.web.dto.CreateAccountDto;
import com.sandbox.server.banking.web.mapper.AccountWebMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountApplicationService accountApplicationService;
    private final AccountWebMapper mapper;

    @GetMapping("/accounts")
    public ResponseEntity<PagedModel<AccountDto>> accounts(
            @PageableDefault(size = 20, sort = "accountNumber") Pageable pageable) {

        return ResponseEntity.ok(new PagedModel<>(accountApplicationService.listAccounts(pageable).map(mapper::toDto)));
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody CreateAccountDto request) {
        var account = accountApplicationService.openAccount(mapper.toAccountNumber(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(account));
    }
}
