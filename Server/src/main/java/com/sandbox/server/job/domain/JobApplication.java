package com.sandbox.server.job.domain;

import java.time.Instant;
import java.util.UUID;

public record JobApplication(
        JobApplicationId id,
        Long version,
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
        return new JobApplication(new JobApplicationId(UUID.randomUUID()), null, companyName, description, rate, paidLeave, cv, now, now, JobApplicationStatus.SENT);
    }

    public JobApplication withStatus(JobApplicationStatus newStatus, Long expectedVersion, Instant now) {
        return new JobApplication(id, expectedVersion, companyName, description, rate, paidLeave, cv, sentAt, now, newStatus);
    }

    public JobApplication withDescription(String newDescription, Long expectedVersion, Instant now) {
        return new JobApplication(id, expectedVersion, companyName, newDescription, rate, paidLeave, cv, sentAt, now, status);
    }

    public JobApplication withUpdate(String newDescription, JobApplicationStatus newStatus, Long expectedVersion, Instant now) {
        return new JobApplication(id, expectedVersion, companyName, newDescription, rate, paidLeave, cv, sentAt, now, newStatus);
    }
}
