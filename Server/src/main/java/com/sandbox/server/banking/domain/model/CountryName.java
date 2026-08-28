package com.sandbox.server.banking.domain.model;

public record CountryName(String value) {

    public CountryName {
        if (value == null || value.isBlank() || value.length() > 50) {
            throw new IllegalArgumentException("Country name must not be blank and must not exceed 50 characters");
        }
    }
}
