package com.sandbox.server.banking.domain.model;

public record Card(CardId id, Integer version, CardNumber cardNumber, AccountId accountId) {
}
