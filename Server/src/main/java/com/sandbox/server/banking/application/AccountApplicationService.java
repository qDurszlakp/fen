package com.sandbox.server.banking.application;

import com.sandbox.server.banking.domain.model.Account;
import com.sandbox.server.banking.domain.model.AccountNumber;
import com.sandbox.server.banking.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountApplicationService {

    private final AccountRepository accountRepository;

    public Page<Account> listAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    public Account openAccount(AccountNumber accountNumber) {
        return accountRepository.save(Account.open(accountNumber));
    }
}
