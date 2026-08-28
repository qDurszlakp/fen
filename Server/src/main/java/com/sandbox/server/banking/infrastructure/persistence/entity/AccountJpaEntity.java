package com.sandbox.server.banking.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persistence model for the {@code ACCOUNTS} table. Deliberately kept separate from the domain
 * {@link com.sandbox.server.banking.domain.model.Account} aggregate: JPA needs a mutable, identity-bearing,
 * no-args-constructible shape, which is exactly what a rich domain model should not be.
 * No {@code cards} association here on purpose - Card is its own aggregate, referencing this one only
 * by id, so the two are never loaded through each other.
 */
@Entity
@Getter
@Setter
@Table(name = "ACCOUNTS")
@NoArgsConstructor
public class AccountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACCOUNT_ID")
    private Long id;

    @Version
    @Column(name = "VERSION")
    private Integer version;

    @Column(name = "ACC_NUMBER")
    private String accountNumber;

}
