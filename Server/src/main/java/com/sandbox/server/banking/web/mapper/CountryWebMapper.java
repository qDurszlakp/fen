package com.sandbox.server.banking.web.mapper;

import com.sandbox.server.banking.domain.model.Country;
import com.sandbox.server.banking.domain.model.CountryCode;
import com.sandbox.server.banking.domain.model.CountryName;
import com.sandbox.server.banking.web.dto.CountryDto;
import com.sandbox.server.banking.web.dto.CreateCountryDto;
import org.springframework.stereotype.Component;

@Component
public class CountryWebMapper {

    public CountryDto toDto(Country country) {
        CountryDto dto = new CountryDto();
        dto.setId(country.id().value());
        dto.setName(country.name().value());
        dto.setCode(country.code().value());
        dto.setInsertTime(country.registeredAt());
        dto.setVersion(country.version());
        return dto;
    }

    public CountryName toName(CreateCountryDto dto) {
        return new CountryName(dto.getName());
    }

    public CountryCode toCode(CreateCountryDto dto) {
        return new CountryCode(dto.getCode());
    }
}
