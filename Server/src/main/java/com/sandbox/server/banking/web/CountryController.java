package com.sandbox.server.banking.web;

import com.sandbox.server.banking.application.CountryApplicationService;
import com.sandbox.server.banking.web.dto.CountryDto;
import com.sandbox.server.banking.web.dto.CreateCountryDto;
import com.sandbox.server.banking.web.mapper.CountryWebMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CountryController {

    private final CountryApplicationService countryApplicationService;
    private final CountryWebMapper mapper;

    @PostMapping("/countries")
    public ResponseEntity<CountryDto> createCountry(@Valid @RequestBody CreateCountryDto request) {
        var country = countryApplicationService.registerCountry(mapper.toName(request), mapper.toCode(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(country));
    }
}
