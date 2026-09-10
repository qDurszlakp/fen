package com.sandbox.server.job.application.port.in;

import com.sandbox.server.job.domain.JobApplication;
import com.sandbox.server.job.domain.JobApplicationId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface FindJobApplicationsUseCase {

    Optional<JobApplication> findById(JobApplicationId id);

    Page<JobApplication> findAll(Pageable pageable);
}
