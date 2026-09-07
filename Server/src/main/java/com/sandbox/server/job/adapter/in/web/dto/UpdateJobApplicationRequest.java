package com.sandbox.server.job.adapter.in.web.dto;

import com.sandbox.server.job.domain.JobApplicationStatus;

public record UpdateJobApplicationRequest(String description, JobApplicationStatus status, Long version) {
}
