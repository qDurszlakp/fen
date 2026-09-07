package com.sandbox.server.job.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public record CreateJobApplicationRequest(
        String companyName,
        String description,
        String rateType,
        BigDecimal rateAmount,
        boolean paidLeave,
        int vacationDays,
        @NotNull MultipartFile cv
) {
}
