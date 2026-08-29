package com.sandbox.server.job.domain;

import java.math.BigDecimal;

public sealed interface Rate permits HourlyRate, MonthlyRate {

    BigDecimal amount();
}
