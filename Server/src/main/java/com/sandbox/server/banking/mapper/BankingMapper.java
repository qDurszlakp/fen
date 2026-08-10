package com.sandbox.server.banking.mapper;

import com.sandbox.server.banking.dto.*;
import com.sandbox.server.banking.entity.Account;
import com.sandbox.server.banking.entity.Card;
import com.sandbox.server.banking.entity.Country;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BankingMapper {

    CountryDto countryToCountryDto(Country country);

    AccountDto accountToAccountDto(Account account);

    @Mapping(source = "account.accountNumber", target = "accountNumber")
    CardDto cardToCardDto(Card card);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "insertTime", ignore = true)
    Country countryDtoToCountry(CreateCountryDto createCountryDto);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "cards", ignore = true)
    })
    Account accountDtoToAccount(CreateAccountDto createAccountDto);
}
