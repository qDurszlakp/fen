package com.sandbox.server.mcp;

import com.sandbox.server.banking.dto.AccountDto;
import com.sandbox.server.banking.dto.CardDto;
import com.sandbox.server.banking.dto.CountryDto;
import com.sandbox.server.banking.dto.CreateCountryDto;
import com.sandbox.server.banking.service.BankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class McpTools {

    private final BankingService bankingService;

    @Tool(description = "Returns the list of all accounts in the database")
    public List<AccountDto> accounts() {
        return bankingService.getAccounts();
    }

    @Tool(description = "Returns the list of all cards in the database")
    public List<CardDto> cards() {
        return bankingService.getCards();
    }

    @Tool(description = "Creates a new country and returns the saved record")
    public CountryDto createCountry(
            @ToolParam(description = "Full country name, e.g. Poland") String name,
            @ToolParam(description = "ISO country code, e.g. 95114020040000300278655181") String code) {
        CreateCountryDto dto = new CreateCountryDto();
        dto.setName(name);
        dto.setCode(code);
        return bankingService.createCountry(dto);
    }
}
