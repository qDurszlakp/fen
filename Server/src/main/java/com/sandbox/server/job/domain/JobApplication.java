package com.sandbox.server.job.domain;

import java.time.Instant;
import java.util.UUID;

public record JobApplication(
        JobApplicationId id,
        CompanyName companyName,
        String description,
        Rate rate,
        PaidLeave paidLeave,
        byte[] cv,
        Instant sentAt,
        Instant updatedAt,
        JobApplicationStatus status
) {

    public static JobApplication submit(CompanyName companyName, String description, Rate rate, PaidLeave paidLeave, byte[] cv, Instant now) {
        return new JobApplication(new JobApplicationId(UUID.randomUUID()), companyName, description, rate, paidLeave, cv, now, now, JobApplicationStatus.SENT);
    }

    public JobApplication withStatus(JobApplicationStatus newStatus, Instant now) {
        return new JobApplication(id, companyName, description, rate, paidLeave, cv, sentAt, now, newStatus);
    }
}
