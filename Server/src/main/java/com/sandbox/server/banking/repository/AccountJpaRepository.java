package com.sandbox.server.banking.repository;

import com.sandbox.server.banking.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountJpaRepository extends JpaRepository<Account, Long> {

}
