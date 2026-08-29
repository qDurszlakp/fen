package com.sandbox.server.job.domain;

public record CompanyName(String value) {

    public CompanyName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Company name must not be blank");
        }
    }
}
