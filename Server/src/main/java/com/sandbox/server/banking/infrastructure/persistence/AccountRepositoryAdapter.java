package com.sandbox.server.banking.infrastructure.persistence;

import com.sandbox.server.banking.domain.model.Account;
import com.sandbox.server.banking.domain.model.AccountId;
import com.sandbox.server.banking.domain.repository.AccountRepository;
import com.sandbox.server.banking.infrastructure.persistence.entity.AccountJpaEntity;
import com.sandbox.server.banking.infrastructure.persistence.mapper.AccountPersistenceMapper;
import com.sandbox.server.banking.infrastructure.persistence.repository.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountPersistenceMapper mapper;

    @Override
    public Account save(Account account) {
        AccountJpaEntity saved = jpaRepository.save(mapper.toEntity(account));
        return mapper.toDomain(saved);
    }

    @Override
    public Page<Account> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }
}
