package com.sandbox.server.job.adapter.in.web.dto;

import com.sandbox.server.job.domain.JobApplicationStatus;

public record UpdateJobApplicationStatusRequest(JobApplicationStatus status, Long version) {
}
