package com.sandbox.server.banking.domain.model;

public record CardNumber(String value) {

    public CardNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Card number must not be blank");
        }
    }
}
