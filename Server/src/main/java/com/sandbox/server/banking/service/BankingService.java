package com.sandbox.server.banking.service;

import com.sandbox.server.banking.dto.*;
import com.sandbox.server.banking.entity.Account;
import com.sandbox.server.banking.entity.Country;
import com.sandbox.server.banking.mapper.BankingMapper;
import com.sandbox.server.banking.repository.AccountJpaRepository;
import com.sandbox.server.banking.repository.CardJpaRepository;
import com.sandbox.server.banking.repository.CountryJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class BankingService {

    private final BankingMapper mapper;
    private final AccountJpaRepository accountJpaRepository;
    private final CardJpaRepository accountCardJpaRepository;
    private final CountryJpaRepository countryJpaRepository;

    public List<AccountDto> getAccounts() {
        return accountJpaRepository.findAll()
                .stream().map(mapper::accountToAccountDto)
                .toList();
    }

    public List<CardDto> getCards() {
        return accountCardJpaRepository.findAll()
                .stream().map(mapper::cardToCardDto)
                .toList();
    }

    public List<CountryDto> getCountries() {
        return null;
    }

    public CountryDto createCountry(CreateCountryDto countryDto) {
        Country entryCountry = mapper.countryDtoToCountry(countryDto);
        entryCountry.setInsertTime(ZonedDateTime.now(ZoneId.of("Europe/Warsaw")));
        Country savedEntity = countryJpaRepository.save(entryCountry);
        return mapper.countryToCountryDto(savedEntity);
    }

    public AccountDto createAccount(CreateAccountDto accountDto) {
        Account savedEntity = accountJpaRepository.save(mapper.accountDtoToAccount(accountDto));
        return mapper.accountToAccountDto(savedEntity);
    }
}
