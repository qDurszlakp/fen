package com.sandbox.server.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardDto {

    @NotNull(message = "Card id is required")
    @Positive(message = "Card id must be positive")
    private Long id;

    @NotNull(message = "Version is required")
    @PositiveOrZero(message = "Version must not be negative")
    private Integer version;

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{16}", message = "Card number must consist of exactly 16 digits")
    private String cardNumber;

    @Pattern(regexp = "\\d{26}", message = "Account number must consist of exactly 26 digits")
    private String accountNumber;
}
