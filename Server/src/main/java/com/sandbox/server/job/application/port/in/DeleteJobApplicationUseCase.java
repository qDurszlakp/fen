package com.sandbox.server.job.application.port.in;

import com.sandbox.server.job.domain.JobApplicationId;

public interface DeleteJobApplicationUseCase {

    void delete(JobApplicationId id);
}
