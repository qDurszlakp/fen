package com.sandbox.server.job.domain;

import java.time.Instant;
import java.util.UUID;

public record JobApplication(
        JobApplicationId id,
        CompanyName companyName,
        Rate rate,
        PaidLeave paidLeave,
        byte[] cv,
        Instant sentAt,
        Instant updatedAt,
        JobApplicationStatus status
) {

    public static JobApplication submit(CompanyName companyName, Rate rate, PaidLeave paidLeave, byte[] cv, Instant now) {
        return new JobApplication(new JobApplicationId(UUID.randomUUID()), companyName, rate, paidLeave, cv, now, now, JobApplicationStatus.SENT);
    }
}
