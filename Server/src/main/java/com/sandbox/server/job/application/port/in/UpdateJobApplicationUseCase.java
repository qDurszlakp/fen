package com.sandbox.server.job.application.port.in;

import com.sandbox.server.job.domain.JobApplication;
import com.sandbox.server.job.domain.JobApplicationId;
import com.sandbox.server.job.domain.JobApplicationStatus;

public interface UpdateJobApplicationUseCase {

    JobApplication updateStatus(JobApplicationId id, Long version, JobApplicationStatus status);

    JobApplication updateDescription(JobApplicationId id, Long version, String description);

    JobApplication update(JobApplicationId id, Long version, String description, JobApplicationStatus status);
}
