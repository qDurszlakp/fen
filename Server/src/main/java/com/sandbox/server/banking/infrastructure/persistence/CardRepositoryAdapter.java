package com.sandbox.server.banking.infrastructure.persistence;

import com.sandbox.server.banking.domain.model.Card;
import com.sandbox.server.banking.domain.repository.CardRepository;
import com.sandbox.server.banking.infrastructure.persistence.mapper.CardPersistenceMapper;
import com.sandbox.server.banking.infrastructure.persistence.repository.CardJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class CardRepositoryAdapter implements CardRepository {

    private final CardJpaRepository jpaRepository;
    private final CardPersistenceMapper mapper;

    @Override
    public Page<Card> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }
}
