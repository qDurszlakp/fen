package com.sandbox.server.banking.repository;

import com.sandbox.server.banking.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardJpaRepository extends JpaRepository<Card, Long> {
}
