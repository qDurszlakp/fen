package com.sandbox.server.banking.domain.model;

/**
 * Aggregate root of the account. Cards are a separate aggregate (see {@link Card}) referencing
 * the account only by {@link AccountId} - they are created, queried and paginated independently,
 * so bundling them inside this aggregate would not match how the domain is actually used.
 */
public record Account(AccountId id, Integer version, AccountNumber accountNumber) {

    public static Account open(AccountNumber accountNumber) {
        return new Account(null, null, accountNumber);
    }
}
