package com.sandbox.server.banking.domain.repository;

import com.sandbox.server.banking.domain.model.Account;
import com.sandbox.server.banking.domain.model.AccountId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AccountRepository {

    Account save(Account account);

    Page<Account> findAll(Pageable pageable);

    Optional<Account> findById(AccountId id);
}
