package com.sandbox.server.banking.infrastructure.persistence.mapper;

import com.sandbox.server.banking.domain.model.AccountId;
import com.sandbox.server.banking.domain.model.Card;
import com.sandbox.server.banking.domain.model.CardId;
import com.sandbox.server.banking.domain.model.CardNumber;
import com.sandbox.server.banking.infrastructure.persistence.entity.CardJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CardPersistenceMapper {

    public Card toDomain(CardJpaEntity entity) {
        return new Card(
                new CardId(entity.getId()),
                entity.getVersion(),
                new CardNumber(entity.getCardNumber()),
                entity.getAccountId() != null ? new AccountId(entity.getAccountId()) : null
        );
    }
}
