package com.sandbox.server.banking.application;

import com.sandbox.server.banking.domain.model.Country;
import com.sandbox.server.banking.domain.model.CountryCode;
import com.sandbox.server.banking.domain.model.CountryName;
import com.sandbox.server.banking.domain.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class CountryApplicationService {

    private final CountryRepository countryRepository;
    private final Clock clock;

    public Country registerCountry(CountryName name, CountryCode code) {
        return countryRepository.save(Country.register(name, code, clock));
    }
}
