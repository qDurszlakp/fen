package com.sandbox.server.banking.domain.model;

/**
 * Value object enforcing the domain invariant that an account number is exactly 26 digits (NRB format).
 * Web layer validation (Bean Validation) gives a friendly 400 early; this is the second line of defense
 * so an invalid number can never exist inside a valid {@link Account} instance.
 */
public record AccountNumber(String value) {

    public AccountNumber {
        if (value == null || !value.matches("\\d{26}")) {
            throw new IllegalArgumentException("Account number must consist of exactly 26 digits");
        }
    }
}
