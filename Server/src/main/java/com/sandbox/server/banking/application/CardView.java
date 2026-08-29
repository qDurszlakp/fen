package com.sandbox.server.banking.application;

import com.sandbox.server.banking.domain.model.AccountNumber;
import com.sandbox.server.banking.domain.model.Card;

public record CardView(Card card, AccountNumber accountNumber) {
}
