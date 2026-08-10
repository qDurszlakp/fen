package com.sandbox.server.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CountryDto {

    @NotNull(message = "Country id is required")
    @Positive(message = "Country id must be positive")
    private Long id;

    @NotBlank(message = "Country name is required")
    @Size(max = 50, message = "Country name must not exceed 50 characters")
    private String name;

    @NotBlank(message = "Country code is required")
    @Size(max = 10, message = "Country code must not exceed 10 characters")
    @Pattern(regexp = "[A-Za-z0-9]+", message = "Country code must be alphanumeric")
    private String code;

    private LocalDateTime insertTime;

    @NotNull(message = "Version is required")
    @PositiveOrZero(message = "Version must not be negative")
    private Integer version;
}
