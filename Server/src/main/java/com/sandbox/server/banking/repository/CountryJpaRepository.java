package com.sandbox.server.banking.repository;

import com.sandbox.server.banking.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryJpaRepository extends JpaRepository<Country, Long> {

    Optional<Country> findCountryByCode(String code);

    Country save(Country entity);
}
