package com.sandbox.server.banking.service;

import com.sandbox.server.banking.dto.*;
import com.sandbox.server.banking.entity.Account;
import com.sandbox.server.banking.entity.Country;
import com.sandbox.server.banking.mapper.BankingMapper;
import com.sandbox.server.banking.repository.AccountJpaRepository;
import com.sandbox.server.banking.repository.CardJpaRepository;
import com.sandbox.server.banking.repository.CountryJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class BankingService {

    private final BankingMapper mapper;
    private final AccountJpaRepository accountJpaRepository;
    private final CardJpaRepository accountCardJpaRepository;
    private final CountryJpaRepository countryJpaRepository;
    private final Clock clock;

    public Page<AccountDto> getAccounts(Pageable pageable) {
        return accountJpaRepository.findAll(pageable)
                .map(mapper::accountToAccountDto);
    }

    public Page<CardDto> getCards(Pageable pageable) {
        return accountCardJpaRepository.findAll(pageable)
                .map(mapper::cardToCardDto);
    }

    public CountryDto createCountry(CreateCountryDto countryDto) {
        Country entryCountry = mapper.countryDtoToCountry(countryDto);
        entryCountry.setInsertTime(Instant.now(clock));
        Country savedEntity = countryJpaRepository.save(entryCountry);
        return mapper.countryToCountryDto(savedEntity);
    }

    public AccountDto createAccount(CreateAccountDto accountDto) {
        Account savedEntity = accountJpaRepository.save(mapper.accountDtoToAccount(accountDto));
        return mapper.accountToAccountDto(savedEntity);
    }
}
