package com.sandbox.server.job.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "job_applications")
public record JobApplicationMongoDocument(

        @Id
        String id,

        @Version
        Long version,

        String companyName,

        String description,

        String rateType,

        BigDecimal rateAmount,

        boolean paidLeave,

        int vacationDays,

        byte[] cv,

        Instant sentAt,

        Instant updatedAt,

        String status
) {
}
