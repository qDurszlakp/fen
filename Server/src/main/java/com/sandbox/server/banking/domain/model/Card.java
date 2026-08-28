package com.sandbox.server.banking.domain.model;

/**
 * Aggregate root of the card, linked to its owning account only by identity ({@link AccountId}).
 */
public record Card(CardId id, Integer version, CardNumber cardNumber, AccountId accountId) {
}
