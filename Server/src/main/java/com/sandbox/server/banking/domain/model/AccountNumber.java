package com.sandbox.server.banking.domain.model;

public record AccountNumber(String value) {

    public AccountNumber {
        if (value == null || !value.matches("\\d{26}")) {
            throw new IllegalArgumentException("Account number must consist of exactly 26 digits");
        }
    }
}
