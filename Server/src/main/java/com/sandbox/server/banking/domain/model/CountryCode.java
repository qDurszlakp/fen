package com.sandbox.server.banking.domain.model;

public record CountryCode(String value) {

    public CountryCode {
        if (value == null || !value.matches("[A-Za-z0-9]{1,10}")) {
            throw new IllegalArgumentException("Country code must be alphanumeric and at most 10 characters");
        }
    }
}
