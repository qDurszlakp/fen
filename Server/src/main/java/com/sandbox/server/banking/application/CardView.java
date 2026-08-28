package com.sandbox.server.banking.application;

import com.sandbox.server.banking.domain.model.AccountNumber;
import com.sandbox.server.banking.domain.model.Card;

/**
 * Read model composing the Card aggregate with the account number of its owning account.
 * Card only knows its {@code AccountId} (aggregates reference each other by identity, never by
 * direct object reference) - resolving the human-readable account number is a cross-aggregate
 * query concern, which belongs here in the application layer, not inside either aggregate.
 */
public record CardView(Card card, AccountNumber accountNumber) {
}
