package com.sandbox.server.job.application.port.in;

import com.sandbox.server.job.domain.CompanyName;
import com.sandbox.server.job.domain.JobApplication;
import com.sandbox.server.job.domain.PaidLeave;
import com.sandbox.server.job.domain.Rate;

public interface SubmitJobApplicationUseCase {

    JobApplication submit(CompanyName companyName, String description, Rate rate, PaidLeave paidLeave, byte[] cv);
}
