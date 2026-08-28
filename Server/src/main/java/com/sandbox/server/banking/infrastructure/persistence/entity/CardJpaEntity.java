package com.sandbox.server.banking.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "CARDS")
@NoArgsConstructor
public class CardJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CARD_ID")
    private Long id;

    @Version
    @Column(name = "VERSION")
    private Integer version;

    @Column(name = "CARD_NUMBER")
    private String cardNumber;

    @Column(name = "ACCOUNT_ID")
    private Long accountId;
}
