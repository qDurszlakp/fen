package com.sandbox.server.banking.infrastructure.persistence;

import com.sandbox.server.banking.domain.model.Country;
import com.sandbox.server.banking.domain.repository.CountryRepository;
import com.sandbox.server.banking.infrastructure.persistence.entity.CountryJpaEntity;
import com.sandbox.server.banking.infrastructure.persistence.mapper.CountryPersistenceMapper;
import com.sandbox.server.banking.infrastructure.persistence.repository.CountryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class CountryRepositoryAdapter implements CountryRepository {

    private final CountryJpaRepository jpaRepository;
    private final CountryPersistenceMapper mapper;

    @Override
    public Country save(Country country) {
        CountryJpaEntity saved = jpaRepository.save(mapper.toEntity(country));
        return mapper.toDomain(saved);
    }
}
