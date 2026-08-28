package com.sandbox.server.banking.infrastructure.persistence.repository;

import com.sandbox.server.banking.infrastructure.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, Long> {
}
