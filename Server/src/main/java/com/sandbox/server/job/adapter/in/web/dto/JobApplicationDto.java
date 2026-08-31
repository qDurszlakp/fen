package com.sandbox.server.job.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record JobApplicationDto(
        UUID id,
        String companyName,
        String description,
        String rateType,
        BigDecimal rateAmount,
        boolean paidLeave,
        int vacationDays,
        Instant sentAt,
        Instant updatedAt,
        String status,
        boolean hasCv
) {
}
