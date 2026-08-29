package com.sandbox.server.job.domain;

import java.math.BigDecimal;

public record HourlyRate(BigDecimal amount) implements Rate {
}
