package com.sandbox.server.job.domain;

import java.math.BigDecimal;

public record MonthlyRate(BigDecimal amount) implements Rate {
}
