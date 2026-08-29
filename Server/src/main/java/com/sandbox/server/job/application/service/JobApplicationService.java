package com.sandbox.server.job.application.service;

import com.sandbox.server.job.application.port.in.SaveJobApplicationUseCase;
import com.sandbox.server.job.application.port.out.SaveJobApplicationPort;
import com.sandbox.server.job.domain.CompanyName;
import com.sandbox.server.job.domain.JobApplication;
import com.sandbox.server.job.domain.PaidLeave;
import com.sandbox.server.job.domain.Rate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JobApplicationService implements SaveJobApplicationUseCase {

    private final SaveJobApplicationPort saveJobApplicationPort;
    private final Clock clock;

    @Override
    public JobApplication submit(CompanyName companyName, Rate rate, PaidLeave paidLeave, byte[] cv) {
        return saveJobApplicationPort.save(JobApplication.submit(companyName, rate, paidLeave, cv, Instant.now(clock)));
    }
}
