package com.sandbox.server.banking.infrastructure.persistence.mapper;

import com.sandbox.server.banking.domain.model.Country;
import com.sandbox.server.banking.domain.model.CountryCode;
import com.sandbox.server.banking.domain.model.CountryId;
import com.sandbox.server.banking.domain.model.CountryName;
import com.sandbox.server.banking.infrastructure.persistence.entity.CountryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CountryPersistenceMapper {

    public Country toDomain(CountryJpaEntity entity) {
        return new Country(
                new CountryId(entity.getId()),
                entity.getVersion(),
                new CountryName(entity.getName()),
                new CountryCode(entity.getCode()),
                entity.getInsertTime()
        );
    }

    public CountryJpaEntity toEntity(Country country) {
        CountryJpaEntity entity = new CountryJpaEntity();
        if (country.id() != null) {
            entity.setId(country.id().value());
        }
        entity.setVersion(country.version());
        entity.setName(country.name().value());
        entity.setCode(country.code().value());
        entity.setInsertTime(country.registeredAt());
        return entity;
    }
}
