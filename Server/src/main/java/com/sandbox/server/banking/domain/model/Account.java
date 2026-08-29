package com.sandbox.server.banking.domain.model;

public record Account(AccountId id, Integer version, AccountNumber accountNumber) {

    public static Account open(AccountNumber accountNumber) {
        return new Account(null, null, accountNumber);
    }
}
