package com.sandbox.server.banking.infrastructure.persistence.mapper;

import com.sandbox.server.banking.domain.model.Account;
import com.sandbox.server.banking.domain.model.AccountId;
import com.sandbox.server.banking.domain.model.AccountNumber;
import com.sandbox.server.banking.infrastructure.persistence.entity.AccountJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Translates between the {@link Account} aggregate and its JPA persistence model. Hand-written on
 * purpose: this boundary constructs value objects, which MapStruct cannot infer without extra
 * configuration - being explicit here is more valuable than saving a few lines of boilerplate.
 */
@Component
public class AccountPersistenceMapper {

    public Account toDomain(AccountJpaEntity entity) {
        return new Account(
                new AccountId(entity.getId()),
                entity.getVersion(),
                new AccountNumber(entity.getAccountNumber())
        );
    }

    public AccountJpaEntity toEntity(Account account) {
        AccountJpaEntity entity = new AccountJpaEntity();
        if (account.id() != null) {
            entity.setId(account.id().value());
        }
        entity.setVersion(account.version());
        entity.setAccountNumber(account.accountNumber().value());
        return entity;
    }
}
