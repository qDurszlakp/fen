package com.sandbox.server.mcp;

import com.sandbox.server.dto.AccountDto;
import com.sandbox.server.dto.CardDto;
import com.sandbox.server.dto.CountryDto;
import com.sandbox.server.dto.CreateCountryDto;
import com.sandbox.server.service.DbService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class McpTools {

    private final DbService dbService;

    @Tool(description = "Returns the list of all accounts in the database")
    public List<AccountDto> accounts() {
        return dbService.getAccounts();
    }

    @Tool(description = "Returns the list of all cards in the database")
    public List<CardDto> cards() {
        return dbService.getCards();
    }

    @Tool(description = "Creates a new country and returns the saved record")
    public CountryDto createCountry(
            @ToolParam(description = "Full country name, e.g. Poland") String name,
            @ToolParam(description = "ISO country code, e.g. 95114020040000300278655181") String code) {
        CreateCountryDto dto = new CreateCountryDto();
        dto.setName(name);
        dto.setCode(code);
        return dbService.createCountry(dto);
    }
}
