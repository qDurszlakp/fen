package com.sandbox.server.job.application.service;

import com.sandbox.server.job.adapter.out.persistence.mapper.port.in.SaveJobApplicationUseCase;
import com.sandbox.server.job.adapter.out.persistence.mapper.port.out.SaveJobApplicationPort;
import com.sandbox.server.job.domain.CompanyName;
import com.sandbox.server.job.domain.JobApplication;
import com.sandbox.server.job.domain.JobApplicationId;
import com.sandbox.server.job.domain.JobApplicationStatus;
import com.sandbox.server.job.domain.PaidLeave;
import com.sandbox.server.job.domain.Rate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class JobApplicationService implements SaveJobApplicationUseCase {

    private final SaveJobApplicationPort saveJobApplicationPort;
    private final Clock clock;

    @Override
    public JobApplication submit(CompanyName companyName, String description, Rate rate, PaidLeave paidLeave, byte[] cv) {
        return saveJobApplicationPort.save(JobApplication.submit(companyName, description, rate, paidLeave, cv, Instant.now(clock)));
    }

    @Override
    public JobApplication updateStatus(JobApplicationId id, JobApplicationStatus status) {
        JobApplication existing = saveJobApplicationPort.findById(id)
                .orElseThrow(() -> new NoSuchElementException("no job application " + id.value()));

        return saveJobApplicationPort.save(existing.withStatus(status, Instant.now(clock)));
    }
}
