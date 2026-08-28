package com.sandbox.server.banking.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountDto {

    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "\\d{26}", message = "Account number must consist of exactly 26 digits")
    private String accountNumber;
}
