package com.sandbox.server.banking.domain.repository;

import com.sandbox.server.banking.domain.model.Account;
import com.sandbox.server.banking.domain.model.AccountId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Domain-owned port for the {@link Account} aggregate. The implementation (adapter) lives in the
 * infrastructure layer, which depends on this interface - not the other way round (dependency inversion).
 * Pageable/Page are kept here as a pragmatic compromise: reimplementing Spring Data's paging contract
 * just to keep the domain framework-free is not worth it in an education sandbox.
 */
public interface AccountRepository {

    Account save(Account account);

    Page<Account> findAll(Pageable pageable);

    Optional<Account> findById(AccountId id);
}
