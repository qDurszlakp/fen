package com.sandbox.server.job.adapter.in.web.mapper;

import com.sandbox.server.job.adapter.in.web.dto.CreateJobApplicationRequest;
import com.sandbox.server.job.adapter.in.web.dto.JobApplicationDto;
import com.sandbox.server.job.domain.CompanyName;
import com.sandbox.server.job.domain.HourlyRate;
import com.sandbox.server.job.domain.JobApplication;
import com.sandbox.server.job.domain.MonthlyRate;
import com.sandbox.server.job.domain.PaidLeave;
import com.sandbox.server.job.domain.Rate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JobApplicationWebMapper {

    public CompanyName toCompanyName(CreateJobApplicationRequest request) {
        return new CompanyName(request.companyName());
    }

    public Rate toRate(CreateJobApplicationRequest request) {
        return "HOURLY".equalsIgnoreCase(request.rateType())
                ? new HourlyRate(request.rateAmount())
                : new MonthlyRate(request.rateAmount());
    }

    public PaidLeave toPaidLeave(CreateJobApplicationRequest request) {
        return new PaidLeave(request.paidLeave(), request.vacationDays());
    }

    public byte[] toCv(CreateJobApplicationRequest request) throws IOException {
        return request.cv() != null ? request.cv().getBytes() : null;
    }

    public JobApplicationDto toDto(JobApplication jobApplication) {
        return new JobApplicationDto(
                jobApplication.id().value(),
                jobApplication.companyName().value(),
                jobApplication.description(),
                jobApplication.rate() instanceof HourlyRate ? "HOURLY" : "MONTHLY",
                jobApplication.rate().amount(),
                jobApplication.paidLeave().paid(),
                jobApplication.paidLeave().days(),
                jobApplication.sentAt(),
                jobApplication.updatedAt(),
                jobApplication.status().name()
        );
    }
}
