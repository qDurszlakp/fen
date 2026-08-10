package com.sandbox.server.audit.repository;

import com.sandbox.server.audit.entity.Audit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditJpaRepository extends JpaRepository<Audit, Long> {
}
