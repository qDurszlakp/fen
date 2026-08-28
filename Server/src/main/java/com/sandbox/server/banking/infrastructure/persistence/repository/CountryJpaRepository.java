package com.sandbox.server.banking.infrastructure.persistence.repository;

import com.sandbox.server.banking.infrastructure.persistence.entity.CountryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryJpaRepository extends JpaRepository<CountryJpaEntity, Long> {
}
