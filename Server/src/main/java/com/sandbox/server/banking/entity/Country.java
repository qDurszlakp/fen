package com.sandbox.server.banking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "COUNTRIES")
@NoArgsConstructor
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COUNTRY_ID")
    private Long id;

    @Column(name = "COUNTRY")
    private String name;

    @Column(name = "CODE")
    private String code;

    @Column(name = "INS_TIME")
    private Instant insertTime;

    @Version
    @Column(name = "VERSION")
    private Integer version;

}
