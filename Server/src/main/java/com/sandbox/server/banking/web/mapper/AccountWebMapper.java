package com.sandbox.server.banking.web.mapper;

import com.sandbox.server.banking.domain.model.Account;
import com.sandbox.server.banking.domain.model.AccountNumber;
import com.sandbox.server.banking.web.dto.AccountDto;
import com.sandbox.server.banking.web.dto.CreateAccountDto;
import org.springframework.stereotype.Component;

@Component
public class AccountWebMapper {

    public AccountDto toDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setId(account.id().value());
        dto.setVersion(account.version());
        dto.setAccountNumber(account.accountNumber().value());
        return dto;
    }

    public AccountNumber toAccountNumber(CreateAccountDto dto) {
        return new AccountNumber(dto.getAccountNumber());
    }
}
