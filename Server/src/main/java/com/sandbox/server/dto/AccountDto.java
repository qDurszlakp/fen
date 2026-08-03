package com.sandbox.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDto {

    @NotNull(message = "Account id is required")
    @Positive(message = "Account id must be positive")
    private Long id;

    @NotNull(message = "Version is required")
    @PositiveOrZero(message = "Version must not be negative")
    private Integer version;

    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "\\d{26}", message = "Account number must consist of exactly 26 digits")
    private String accountNumber;

}
