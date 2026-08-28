package com.sandbox.server.banking.domain.repository;

import com.sandbox.server.banking.domain.model.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardRepository {

    Page<Card> findAll(Pageable pageable);
}
