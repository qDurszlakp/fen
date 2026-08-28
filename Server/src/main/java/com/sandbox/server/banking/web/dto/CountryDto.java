package com.sandbox.server.banking.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CountryDto {

    private Long id;

    private String name;

    private String code;

    private Instant insertTime;

    private Integer version;
}
