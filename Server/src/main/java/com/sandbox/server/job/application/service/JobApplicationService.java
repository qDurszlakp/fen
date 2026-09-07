package com.sandbox.server.job.application.service;

import com.sandbox.server.job.adapter.out.persistence.mapper.port.in.DeleteJobApplicationUseCase;
import com.sandbox.server.job.adapter.out.persistence.mapper.port.in.FindJobApplicationsUseCase;
import com.sandbox.server.job.adapter.out.persistence.mapper.port.in.SubmitJobApplicationUseCase;
import com.sandbox.server.job.adapter.out.persistence.mapper.port.in.UpdateJobApplicationUseCase;
import com.sandbox.server.job.adapter.out.persistence.mapper.port.out.SaveJobApplicationPort;
import com.sandbox.server.job.domain.CompanyName;
import com.sandbox.server.job.domain.JobApplication;
import com.sandbox.server.job.domain.JobApplicationId;
import com.sandbox.server.job.domain.JobApplicationStatus;
import com.sandbox.server.job.domain.PaidLeave;
import com.sandbox.server.job.domain.Rate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobApplicationService implements SubmitJobApplicationUseCase, UpdateJobApplicationUseCase, FindJobApplicationsUseCase, DeleteJobApplicationUseCase {

    private final SaveJobApplicationPort saveJobApplicationPort;
    private final Clock clock;

    @Override
    public JobApplication submit(CompanyName companyName, String description, Rate rate, PaidLeave paidLeave, byte[] cv) {
        return saveJobApplicationPort.save(JobApplication.submit(companyName, description, rate, paidLeave, cv, Instant.now(clock)));
    }

    @Override
    public JobApplication updateStatus(JobApplicationId id, Long version, JobApplicationStatus status) {
        JobApplication existing = saveJobApplicationPort.findById(id)
                .orElseThrow(() -> new NoSuchElementException("no job application " + id.value()));

        return saveJobApplicationPort.save(existing.withStatus(status, version, Instant.now(clock)));
    }

    @Override
    public JobApplication updateDescription(JobApplicationId id, Long version, String description) {
        JobApplication existing = saveJobApplicationPort.findById(id)
                .orElseThrow(() -> new NoSuchElementException("no job application " + id.value()));

        return saveJobApplicationPort.save(existing.withDescription(description, version, Instant.now(clock)));
    }

    @Override
    public JobApplication update(JobApplicationId id, Long version, String description, JobApplicationStatus status) {
        JobApplication existing = saveJobApplicationPort.findById(id)
                .orElseThrow(() -> new NoSuchElementException("no job application " + id.value()));

        return saveJobApplicationPort.save(existing.withUpdate(description, status, version, Instant.now(clock)));
    }

    @Override
    public void delete(JobApplicationId id) {
        saveJobApplicationPort.findById(id)
                .orElseThrow(() -> new NoSuchElementException("no job application " + id.value()));

        saveJobApplicationPort.deleteById(id);
    }

    @Override
    public Optional<JobApplication> findById(JobApplicationId id) {
        return saveJobApplicationPort.findById(id);
    }

    @Override
    public Page<JobApplication> findAll(Pageable pageable) {
        return saveJobApplicationPort.findAll(pageable);
    }
}
