package com.sandbox.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCountryDto {

    @NotBlank(message = "Country name is required")
    @Size(max = 50, message = "Country name must not exceed 50 characters")
    private String name;

    @NotBlank(message = "Country code is required")
    @Size(max = 10, message = "Country code must not exceed 10 characters")
    @Pattern(regexp = "[A-Za-z0-9]+", message = "Country code must be alphanumeric")
    private String code;
}
