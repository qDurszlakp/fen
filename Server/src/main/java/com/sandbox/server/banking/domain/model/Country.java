package com.sandbox.server.banking.domain.model;

import java.time.Clock;
import java.time.Instant;

public record Country(CountryId id, Integer version, CountryName name, CountryCode code, Instant registeredAt) {

    public static Country register(CountryName name, CountryCode code, Clock clock) {
        return new Country(null, null, name, code, Instant.now(clock));
    }
}
