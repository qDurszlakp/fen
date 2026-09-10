package com.sandbox.server.job.adapter.out.persistence.mapper;

import com.sandbox.server.job.adapter.out.persistence.JobApplicationMongoDocument;
import com.sandbox.server.job.domain.CompanyName;
import com.sandbox.server.job.domain.HourlyRate;
import com.sandbox.server.job.domain.JobApplication;
import com.sandbox.server.job.domain.JobApplicationId;
import com.sandbox.server.job.domain.JobApplicationStatus;
import com.sandbox.server.job.domain.MonthlyRate;
import com.sandbox.server.job.domain.PaidLeave;
import com.sandbox.server.job.domain.Rate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JobApplicationPersistenceMapper {

    public JobApplication toDomain(JobApplicationMongoDocument document) {
        Rate rate = "HOURLY".equals(document.rateType())
                ? new HourlyRate(document.rateAmount())
                : new MonthlyRate(document.rateAmount());

        return new JobApplication(
                new JobApplicationId(UUID.fromString(document.id())),
                document.version(),
                new CompanyName(document.companyName()),
                document.description(),
                rate,
                new PaidLeave(document.paidLeave(), document.vacationDays()),
                document.cv(),
                document.sentAt(),
                document.updatedAt(),
                JobApplicationStatus.valueOf(document.status())
        );
    }

    public JobApplicationMongoDocument toDocument(JobApplication jobApplication) {
        return new JobApplicationMongoDocument(
                jobApplication.id().value().toString(),
                jobApplication.version(),
                jobApplication.companyName().value(),
                jobApplication.description(),
                jobApplication.rate() instanceof HourlyRate ? "HOURLY" : "MONTHLY",
                jobApplication.rate().amount(),
                jobApplication.paidLeave().paid(),
                jobApplication.paidLeave().days(),
                jobApplication.cv(),
                jobApplication.sentAt(),
                jobApplication.updatedAt(),
                jobApplication.status().name()
        );
    }
}
