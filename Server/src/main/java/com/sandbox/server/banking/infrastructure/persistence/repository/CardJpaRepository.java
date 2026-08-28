package com.sandbox.server.banking.infrastructure.persistence.repository;

import com.sandbox.server.banking.infrastructure.persistence.entity.CardJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardJpaRepository extends JpaRepository<CardJpaEntity, Long> {
}
