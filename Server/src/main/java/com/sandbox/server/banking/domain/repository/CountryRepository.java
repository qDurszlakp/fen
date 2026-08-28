package com.sandbox.server.banking.domain.repository;

import com.sandbox.server.banking.domain.model.Country;

public interface CountryRepository {

    Country save(Country country);
}
