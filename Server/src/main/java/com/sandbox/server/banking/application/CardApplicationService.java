package com.sandbox.server.banking.application;

import com.sandbox.server.banking.domain.model.Account;
import com.sandbox.server.banking.domain.repository.AccountRepository;
import com.sandbox.server.banking.domain.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardApplicationService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;

    public Page<CardView> listCards(Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(card -> new CardView(card, accountRepository.findById(card.accountId())
                        .map(Account::accountNumber)
                        .orElse(null)));
    }
}
